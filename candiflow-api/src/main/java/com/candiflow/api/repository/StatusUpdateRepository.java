package com.candiflow.api.repository;

import com.candiflow.api.model.entity.Application;
import com.candiflow.api.model.entity.StatusUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatusUpdateRepository extends JpaRepository<StatusUpdate, UUID> {
    
    /**
     * Trouve toutes les mises à jour de statut pour une candidature, triées par date
     */
    List<StatusUpdate> findByApplicationOrderByEventDateDesc(Application application);
    
    /**
     * Trouve une mise à jour de statut par son ID et la candidature associée
     */
    Optional<StatusUpdate> findByIdAndApplication(UUID id, Application application);
    
    /**
     * Trouve la dernière mise à jour de statut pour une candidature
     */
    @Query("SELECT su FROM StatusUpdate su WHERE su.application = ?1 ORDER BY su.eventDate DESC LIMIT 1")
    Optional<StatusUpdate> findLatestByApplication(Application application);
    
    /**
     * Compte le nombre de mises à jour de statut pour une candidature
     */
    long countByApplication(Application application);
}
