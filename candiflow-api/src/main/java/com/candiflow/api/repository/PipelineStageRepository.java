package com.candiflow.api.repository;

import com.candiflow.api.model.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {
    
    /**
     * Trouve un stage par son nom
     */
    Optional<PipelineStage> findByName(String name);
    
    /**
     * Vérifie si un stage existe par son nom
     */
    boolean existsByName(String name);
    
    /**
     * Trouve tous les stages actifs, triés par ordre d'affichage
     */
    List<PipelineStage> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * Trouve le stage par défaut pour les nouveaux candidats (généralement "Nouveau")
     */
    default Optional<PipelineStage> findDefaultStage() {
        return findByName("Nouveau");
    }
    
    /**
     * Trouve tous les stages finaux (is_end_stage = true)
     */
    List<PipelineStage> findByIsEndStageTrue();
}
