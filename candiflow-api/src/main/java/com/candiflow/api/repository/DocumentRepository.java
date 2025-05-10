package com.candiflow.api.repository;

import com.candiflow.api.model.entity.Application;
import com.candiflow.api.model.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    
    /**
     * Trouve tous les documents pour une candidature spécifique
     */
    List<Document> findByApplication(Application application);
    
    /**
     * Trouve tous les documents pour une candidature spécifique avec pagination
     */
    Page<Document> findByApplication(Application application, Pageable pageable);
    
    /**
     * Trouve un document par son ID et la candidature associée
     */
    Optional<Document> findByIdAndApplication(UUID id, Application application);
    
    /**
     * Trouve des documents par type de fichier
     */
    List<Document> findByApplicationAndFileType(Application application, String fileType);
    
    /**
     * Compte le nombre de documents pour une candidature
     */
    long countByApplication(Application application);
    
    /**
     * Vérifie si un document avec le même nom existe déjà pour une candidature
     */
    boolean existsByApplicationAndFileName(Application application, String fileName);
}
