package com.candiflow.api.repository;

import com.candiflow.api.model.entity.CandidateSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateSourceRepository extends JpaRepository<CandidateSource, UUID> {
    
    /**
     * Trouve une source par son nom
     */
    Optional<CandidateSource> findByName(String name);
    
    /**
     * Vérifie si une source existe par son nom
     */
    boolean existsByName(String name);
    
    /**
     * Trouve toutes les sources actives
     */
    List<CandidateSource> findByIsActiveTrue();
    
    /**
     * Trouve la source par défaut (généralement "Autre")
     */
    default Optional<CandidateSource> findDefaultSource() {
        return findByName("Autre");
    }
}
