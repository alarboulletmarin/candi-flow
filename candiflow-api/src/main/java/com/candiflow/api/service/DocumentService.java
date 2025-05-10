package com.candiflow.api.service;

import com.candiflow.api.dto.candidate.DocumentResponse;
import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.Application;
import com.candiflow.api.model.entity.Document;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.repository.ApplicationRepository;
import com.candiflow.api.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

/**
 * Service pour la gestion des documents
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    
    @Value("${app.upload.dir:${user.home}/candiflow/uploads}")
    private String uploadDir;
    
    /**
     * Récupère tous les documents pour une candidature avec pagination
     * @param applicationId ID de la candidature
     * @param pageable Informations de pagination
     * @return Page de documents
     */
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getAllDocumentsByApplication(UUID applicationId, Pageable pageable) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + applicationId));
        
        return documentRepository.findByApplication(application, pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Récupère un document par son ID
     * @param applicationId ID de la candidature
     * @param documentId ID du document
     * @return Le document si trouvé
     */
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID applicationId, UUID documentId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + applicationId));
        
        Document document = documentRepository.findByIdAndApplication(documentId, application)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'ID: " + documentId));
        
        return mapToResponse(document);
    }
    
    /**
     * Télécharge un document
     * @param applicationId ID de la candidature
     * @param documentId ID du document
     * @return La ressource du document
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(UUID applicationId, UUID documentId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + applicationId));
        
        Document document = documentRepository.findByIdAndApplication(documentId, application)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'ID: " + documentId));
        
        try {
            Path filePath = Paths.get(document.getStoragePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Impossible de lire le fichier: " + document.getFileName());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Téléverse un nouveau document pour une candidature
     * @param applicationId ID de la candidature
     * @param file Fichier à téléverser
     * @param user Utilisateur effectuant le téléversement
     * @return Le document créé
     */
    @Transactional
    public DocumentResponse uploadDocument(UUID applicationId, MultipartFile file, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + applicationId));
        
        // Vérifier que l'utilisateur est bien le propriétaire de la candidature
        if (!application.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à téléverser des documents pour cette candidature");
        }
        
        // Vérifier si un document avec le même nom existe déjà
        if (documentRepository.existsByApplicationAndFileName(application, file.getOriginalFilename())) {
            throw new IllegalStateException("Un document avec ce nom existe déjà pour cette candidature");
        }
        
        try {
            // Créer le répertoire de stockage s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir, applicationId.toString());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Générer un nom de fichier unique
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            
            // Copier le fichier dans le répertoire de stockage
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Créer l'entité Document
            Document document = new Document();
            document.setApplication(application);
            document.setFileName(file.getOriginalFilename());
            document.setStoragePath(filePath.toString());
            document.setFileType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setUploadedAt(Instant.now());
            
            Document savedDocument = documentRepository.save(document);
            return mapToResponse(savedDocument);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de stocker le fichier: " + e.getMessage());
        }
    }
    
    /**
     * Supprime un document
     * @param applicationId ID de la candidature
     * @param documentId ID du document
     * @param user Utilisateur effectuant la suppression
     */
    @Transactional
    public void deleteDocument(UUID applicationId, UUID documentId, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + applicationId));
        
        // Vérifier que l'utilisateur est bien le propriétaire de la candidature
        if (!application.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à supprimer des documents pour cette candidature");
        }
        
        Document document = documentRepository.findByIdAndApplication(documentId, application)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'ID: " + documentId));
        
        try {
            // Supprimer le fichier physique
            Path filePath = Paths.get(document.getStoragePath());
            Files.deleteIfExists(filePath);
            
            // Supprimer l'entité Document
            documentRepository.delete(document);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de supprimer le fichier: " + e.getMessage());
        }
    }
    
    /**
     * Convertit une entité Document en DTO DocumentResponse
     */
    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .applicationId(document.getApplication().getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .downloadUrl("/api/applications/" + document.getApplication().getId() + "/documents/" + document.getId() + "/download")
                .build();
    }
}
