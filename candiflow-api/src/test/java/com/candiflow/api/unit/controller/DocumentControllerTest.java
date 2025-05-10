package com.candiflow.api.unit.controller;

import com.candiflow.api.controller.DocumentController;
import com.candiflow.api.dto.candidate.DocumentResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.DocumentService;
import com.candiflow.api.service.UserService;
import com.candiflow.api.unit.BaseUnitTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;
    
    @Mock
    private DocumentService documentService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private DocumentController documentController;

    private UUID applicationId;
    private UUID documentId;
    private User testUser;
    private DocumentResponse testDocumentResponse;

    @BeforeEach
    void setUp() {
        // Initialiser les IDs et les objets de test
        applicationId = UUID.randomUUID();
        documentId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole(UserRole.CANDIDATE);
        
        testDocumentResponse = DocumentResponse.builder()
                .id(documentId)
                .applicationId(applicationId)
                .fileName("test-document.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .uploadedAt(Instant.now())
                .downloadUrl("/api/applications/" + applicationId + "/documents/" + documentId + "/download")
                .build();
        
        // Configure ObjectMapper pour gérer les dates Java 8
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        
        // Configurer MockMvc avec les convertisseurs nécessaires
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        // Configurer le mock pour simuler l'authentification
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(documentController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void getAllDocumentsByApplication_ShouldReturnDocuments() throws Exception {
        // Arrange
        Page<DocumentResponse> documentPage = new PageImpl<>(Collections.singletonList(testDocumentResponse));
        when(documentService.getAllDocumentsByApplication(eq(applicationId), any(Pageable.class))).thenReturn(documentPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/applications/{applicationId}/documents", applicationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(documentId.toString()))
                .andExpect(jsonPath("$.content[0].fileName").value("test-document.pdf"));

        verify(documentService).getAllDocumentsByApplication(eq(applicationId), any(Pageable.class));
    }

    @Test
    void getDocumentById_ShouldReturnDocument() throws Exception {
        // Arrange
        when(documentService.getDocumentById(applicationId, documentId)).thenReturn(testDocumentResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/applications/{applicationId}/documents/{documentId}", applicationId, documentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId.toString()))
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"));

        verify(documentService).getDocumentById(applicationId, documentId);
    }

    @Test
    void downloadDocument_ShouldReturnFileResource() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);
        
        when(documentService.getDocumentById(applicationId, documentId)).thenReturn(testDocumentResponse);
        when(documentService.downloadDocument(applicationId, documentId)).thenReturn(resource);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/applications/{applicationId}/documents/{documentId}/download", applicationId, documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test-document.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(fileContent));

        verify(documentService).getDocumentById(applicationId, documentId);
        verify(documentService).downloadDocument(applicationId, documentId);
    }

    @Test
    void uploadDocument_ShouldCreateAndReturnDocument() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-upload.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(documentService.uploadDocument(eq(applicationId), any(), eq(testUser))).thenReturn(testDocumentResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/applications/{applicationId}/documents", applicationId)
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(documentId.toString()))
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"));

        verify(userService).getUserByEmail("test@example.com");
        verify(documentService).uploadDocument(eq(applicationId), any(), eq(testUser));
    }

    @Test
    void deleteDocument_ShouldDeleteDocument() throws Exception {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/applications/{applicationId}/documents/{documentId}", applicationId, documentId))
                .andExpect(status().isNoContent());

        verify(userService).getUserByEmail("test@example.com");
        verify(documentService).deleteDocument(applicationId, documentId, testUser);
    }
}
