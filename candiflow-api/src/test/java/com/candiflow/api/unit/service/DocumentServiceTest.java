package com.candiflow.api.unit.service;

import com.candiflow.api.dto.candidate.DocumentResponse;
import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.Application;
import com.candiflow.api.model.entity.Document;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.ApplicationRepository;
import com.candiflow.api.repository.DocumentRepository;
import com.candiflow.api.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private DocumentService documentService;

    private User testUser;
    private Application testApplication;
    private Document testDocument;
    private UUID applicationId;
    private UUID documentId;
    private String tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Créer un répertoire temporaire pour les tests
        tempDir = System.getProperty("java.io.tmpdir") + "/candiflow-test-" + UUID.randomUUID();
        Path tempPath = Paths.get(tempDir);
        Files.createDirectories(tempPath);
        
        // Injecter le répertoire temporaire dans le service
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir);
        
        // Initialiser les données de test
        applicationId = UUID.randomUUID();
        documentId = UUID.randomUUID();
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole(UserRole.CANDIDATE);
        
        testApplication = new Application();
        testApplication.setId(applicationId);
        testApplication.setUser(testUser);
        
        testDocument = new Document();
        testDocument.setId(documentId);
        testDocument.setApplication(testApplication);
        testDocument.setFileName("test-document.pdf");
        testDocument.setFileType("application/pdf");
        testDocument.setFileSize(1024L);
        testDocument.setStoragePath(tempDir + "/test-document.pdf");
        testDocument.setUploadedAt(Instant.now());
    }

    @Test
    void getAllDocumentsByApplication_ShouldReturnPageOfDocuments() {
        // Arrange
        Page<Document> documentPage = new PageImpl<>(Collections.singletonList(testDocument));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(documentRepository.findByApplication(eq(testApplication), any(Pageable.class))).thenReturn(documentPage);
        
        // Act
        Page<DocumentResponse> result = documentService.getAllDocumentsByApplication(applicationId, Pageable.unpaged());
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testDocument.getFileName(), result.getContent().get(0).getFileName());
        verify(applicationRepository).findById(applicationId);
        verify(documentRepository).findByApplication(eq(testApplication), any(Pageable.class));
    }
    
    @Test
    void getAllDocumentsByApplication_WhenApplicationNotFound_ShouldThrowException() {
        // Arrange
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            documentService.getAllDocumentsByApplication(applicationId, Pageable.unpaged())
        );
        verify(applicationRepository).findById(applicationId);
        verifyNoInteractions(documentRepository);
    }
    
    @Test
    void getDocumentById_ShouldReturnDocument() {
        // Arrange
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(documentRepository.findByIdAndApplication(documentId, testApplication)).thenReturn(Optional.of(testDocument));
        
        // Act
        DocumentResponse result = documentService.getDocumentById(applicationId, documentId);
        
        // Assert
        assertNotNull(result);
        assertEquals(testDocument.getFileName(), result.getFileName());
        assertEquals(testDocument.getFileType(), result.getFileType());
        assertEquals(testDocument.getFileSize(), result.getFileSize());
        verify(applicationRepository).findById(applicationId);
        verify(documentRepository).findByIdAndApplication(documentId, testApplication);
    }
    
    @Test
    void getDocumentById_WhenDocumentNotFound_ShouldThrowException() {
        // Arrange
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(documentRepository.findByIdAndApplication(documentId, testApplication)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            documentService.getDocumentById(applicationId, documentId)
        );
        verify(applicationRepository).findById(applicationId);
        verify(documentRepository).findByIdAndApplication(documentId, testApplication);
    }
    
    @Test
    void uploadDocument_ShouldCreateAndReturnDocument() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "file", 
            "test-upload.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(documentRepository.existsByApplicationAndFileName(testApplication, file.getOriginalFilename())).thenReturn(false);
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // Act
        DocumentResponse result = documentService.uploadDocument(applicationId, file, testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testDocument.getFileName(), result.getFileName());
        verify(applicationRepository).findById(applicationId);
        verify(documentRepository).existsByApplicationAndFileName(testApplication, file.getOriginalFilename());
        verify(documentRepository).save(any(Document.class));
    }
    
    @Test
    void uploadDocument_WhenFileAlreadyExists_ShouldThrowException() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "file", 
            "test-upload.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(documentRepository.existsByApplicationAndFileName(testApplication, file.getOriginalFilename())).thenReturn(true);
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            documentService.uploadDocument(applicationId, file, testUser)
        );
        verify(applicationRepository).findById(applicationId);
        verify(documentRepository).existsByApplicationAndFileName(testApplication, file.getOriginalFilename());
        verifyNoMoreInteractions(documentRepository);
    }
    
    @Test
    void uploadDocument_WhenUserIsNotOwner_ShouldThrowException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "test-upload.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            documentService.uploadDocument(applicationId, file, otherUser)
        );
        verify(applicationRepository).findById(applicationId);
        verifyNoInteractions(documentRepository);
    }
    
    @Test
    void deleteDocument_ShouldDeleteDocument() throws IOException {
        // Arrange
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(documentRepository.findByIdAndApplication(documentId, testApplication)).thenReturn(Optional.of(testDocument));
        
        // Créer un fichier temporaire pour le test
        Path filePath = Paths.get(testDocument.getStoragePath());
        Files.createFile(filePath);
        
        // Act
        documentService.deleteDocument(applicationId, documentId, testUser);
        
        // Assert
        verify(applicationRepository).findById(applicationId);
        verify(documentRepository).findByIdAndApplication(documentId, testApplication);
        verify(documentRepository).delete(testDocument);
        assertFalse(Files.exists(filePath));
    }
    
    @Test
    void deleteDocument_WhenUserIsNotOwner_ShouldThrowException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            documentService.deleteDocument(applicationId, documentId, otherUser)
        );
        verify(applicationRepository).findById(applicationId);
        verifyNoInteractions(documentRepository);
    }
}
