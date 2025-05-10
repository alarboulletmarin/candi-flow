package com.candiflow.api.repository;

import com.candiflow.api.model.entity.Application;
import com.candiflow.api.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    
    /**
     * Trouve toutes les candidatures d'un utilisateur
     */
    List<Application> findByUserOrderByDateAppliedDesc(User user);
    
    /**
     * Trouve toutes les candidatures d'un utilisateur avec pagination
     */
    Page<Application> findByUser(User user, Pageable pageable);
    
    /**
     * Trouve une candidature par son ID et l'utilisateur associé
     */
    Optional<Application> findByIdAndUser(UUID id, User user);
    
    /**
     * Recherche des candidatures par nom d'entreprise ou titre de poste
     */
    @Query("SELECT a FROM Application a WHERE a.user = ?1 AND (LOWER(a.companyName) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(a.jobTitle) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<Application> searchByUserAndKeyword(User user, String keyword, Pageable pageable);
    
    /**
     * Compte le nombre de candidatures pour un utilisateur
     */
    long countByUser(User user);
}
