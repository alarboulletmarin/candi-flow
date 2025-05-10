package com.candiflow.api.repository;

import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.PipelineStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpeningApplicantRepository extends JpaRepository<OpeningApplicant, UUID>, JpaSpecificationExecutor<OpeningApplicant> {
    
    /**
     * Trouve tous les candidats pour une offre d'emploi spécifique
     */
    List<OpeningApplicant> findByJobOpening(JobOpening jobOpening);
    
    /**
     * Trouve tous les candidats pour une offre d'emploi spécifique avec pagination
     */
    Page<OpeningApplicant> findByJobOpening(JobOpening jobOpening, Pageable pageable);
    
    /**
     * Trouve un candidat par son ID et l'offre d'emploi associée
     */
    Optional<OpeningApplicant> findByIdAndJobOpening(UUID id, JobOpening jobOpening);
    
    /**
     * Trouve tous les candidats à une étape spécifique du pipeline
     */
    List<OpeningApplicant> findByCurrentStage(PipelineStage currentStage);
    
    /**
     * Trouve tous les candidats à une étape spécifique du pipeline pour une offre d'emploi
     */
    List<OpeningApplicant> findByJobOpeningAndCurrentStage(JobOpening jobOpening, PipelineStage currentStage);
    
    /**
     * Recherche des candidats par nom ou email
     */
    @Query("SELECT a FROM OpeningApplicant a WHERE a.jobOpening = ?1 AND (LOWER(a.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<OpeningApplicant> searchByJobOpeningAndKeyword(JobOpening jobOpening, String keyword, Pageable pageable);
    
    /**
     * Vérifie si un candidat existe déjà avec le même email pour une offre d'emploi
     */
    boolean existsByJobOpeningAndEmail(JobOpening jobOpening, String email);
    
    /**
     * Compte le nombre de candidats pour une offre d'emploi
     */
    long countByJobOpening(JobOpening jobOpening);
    
    /**
     * Compte le nombre de candidats à une étape spécifique du pipeline pour une offre d'emploi
     */
    long countByJobOpeningAndCurrentStage(JobOpening jobOpening, PipelineStage currentStage);
}
