package com.candiflow.api.strategy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Interface pour les stratégies de recherche
 * Implémente le pattern Strategy pour rendre le code plus modulaire et extensible
 * @param <T> Type d'entité à rechercher
 * @param <R> Type de réponse à retourner
 */
public interface SearchStrategy<T, R> {
    
    /**
     * Crée une spécification pour la recherche
     * @return Spécification pour la recherche
     */
    Specification<T> createSpecification();
    
    /**
     * Convertit une entité en DTO de réponse
     * @param entity Entité à convertir
     * @return DTO de réponse
     */
    R convertToResponse(T entity);
    
    /**
     * Exécute la recherche avec la spécification et la pagination
     * @param pageable Pagination
     * @return Page de résultats
     */
    Page<R> execute(Pageable pageable);
}
