package com.candiflow.api.repository;

import com.candiflow.api.model.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationStatusRepository extends JpaRepository<ApplicationStatus, UUID> {
    
    /**
     * Trouve un statut par son nom
     */
    Optional<ApplicationStatus> findByName(String name);
    
    /**
     * Vérifie si un statut existe par son nom
     */
    boolean existsByName(String name);
    
    /**
     * Trouve tous les statuts actifs, triés par ordre d'affichage
     */
    List<ApplicationStatus> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * Trouve le statut par défaut pour les nouvelles candidatures (généralement "Postulé")
     * Note: Ceci suppose qu'il y a un statut avec le nom "Postulé"
     */
    default Optional<ApplicationStatus> findDefaultStatus() {
        return findByName("Postulé");
    }
}
