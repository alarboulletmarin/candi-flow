package com.candiflow.api.repository;

import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.RecruiterNote;
import com.candiflow.api.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecruiterNoteRepository extends JpaRepository<RecruiterNote, UUID> {
    
    /**
     * Trouve toutes les notes pour un candidat spécifique
     */
    List<RecruiterNote> findByApplicantOrderByCreatedAtDesc(OpeningApplicant applicant);
    
    /**
     * Trouve toutes les notes pour un candidat spécifique avec pagination
     */
    Page<RecruiterNote> findByApplicant(OpeningApplicant applicant, Pageable pageable);
    
    /**
     * Trouve une note par son ID et le candidat associé
     */
    Optional<RecruiterNote> findByIdAndApplicant(UUID id, OpeningApplicant applicant);
    
    /**
     * Trouve toutes les notes écrites par un recruteur spécifique
     */
    List<RecruiterNote> findByAuthor(User author);
    
    /**
     * Trouve toutes les notes écrites par un recruteur pour un candidat spécifique
     */
    List<RecruiterNote> findByAuthorAndApplicant(User author, OpeningApplicant applicant);
    
    /**
     * Compte le nombre de notes pour un candidat
     */
    long countByApplicant(OpeningApplicant applicant);
}
