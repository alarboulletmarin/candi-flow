package com.candiflow.api.repository;

import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
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
public interface JobOpeningRepository extends JpaRepository<JobOpening, UUID>, JpaSpecificationExecutor<JobOpening> {
    
    /**
     * Trouve toutes les offres d'emploi créées par un recruteur
     */
    List<JobOpening> findByRecruiterOrderByCreatedAtDesc(User recruiter);
    
    /**
     * Trouve toutes les offres d'emploi créées par un recruteur avec pagination
     */
    Page<JobOpening> findByRecruiter(User recruiter, Pageable pageable);
    
    /**
     * Trouve une offre d'emploi par son ID et le recruteur associé
     */
    Optional<JobOpening> findByIdAndRecruiter(UUID id, User recruiter);
    
    /**
     * Trouve toutes les offres d'emploi avec un statut spécifique
     */
    List<JobOpening> findByStatus(JobStatus status);
    
    /**
     * Trouve toutes les offres d'emploi avec un statut spécifique et créées par un recruteur
     */
    Page<JobOpening> findByRecruiterAndStatus(User recruiter, JobStatus status, Pageable pageable);
    
    /**
     * Recherche des offres d'emploi par titre
     */
    @Query("SELECT j FROM JobOpening j WHERE j.recruiter = ?1 AND LOWER(j.title) LIKE LOWER(CONCAT('%', ?2, '%'))")
    Page<JobOpening> searchByRecruiterAndKeyword(User recruiter, String keyword, Pageable pageable);
    
    /**
     * Compte le nombre d'offres d'emploi pour un recruteur
     */
    long countByRecruiter(User recruiter);
}
