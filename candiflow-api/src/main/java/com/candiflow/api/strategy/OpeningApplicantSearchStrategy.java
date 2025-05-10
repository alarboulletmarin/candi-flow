package com.candiflow.api.strategy;

import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.factory.OpeningApplicantResponseFactory;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.repository.OpeningApplicantRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stratégie de recherche pour les candidats
 * Implémente le pattern Strategy
 */
@Data
@Builder
public class OpeningApplicantSearchStrategy implements SearchStrategy<OpeningApplicant, OpeningApplicantResponse> {
    
    private final OpeningApplicantRepository openingApplicantRepository;
    private final OpeningApplicantResponseFactory openingApplicantResponseFactory;
    
    // Critères de recherche
    private String name;
    private String email;
    private String phone;
    private UUID jobOpeningId;
    private UUID stageId;
    private UUID sourceId;
    private LocalDate applicationDateAfter;
    private LocalDate applicationDateBefore;
    
    @Override
    public Specification<OpeningApplicant> createSpecification() {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtre par nom (recherche partielle, insensible à la casse)
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"));
            }
            
            // Filtre par email (recherche partielle, insensible à la casse)
            if (email != null && !email.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"));
            }
            
            // Filtre par téléphone
            if (phone != null && !phone.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("phone"),
                        "%" + phone + "%"));
            }
            
            // Filtre par offre d'emploi
            if (jobOpeningId != null) {
                predicates.add(criteriaBuilder.equal(root.get("jobOpening").get("id"), jobOpeningId));
            }
            
            // Filtre par étape
            if (stageId != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentStage").get("id"), stageId));
            }
            
            // Filtre par source
            if (sourceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("source").get("id"), sourceId));
            }
            
            // Filtre par date de candidature (après)
            if (applicationDateAfter != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("applicationDate"), applicationDateAfter));
            }
            
            // Filtre par date de candidature (avant)
            if (applicationDateBefore != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("applicationDate"), applicationDateBefore));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    @Override
    public OpeningApplicantResponse convertToResponse(OpeningApplicant entity) {
        return openingApplicantResponseFactory.createFromEntity(entity);
    }
    
    @Override
    public Page<OpeningApplicantResponse> execute(Pageable pageable) {
        Specification<OpeningApplicant> spec = createSpecification();
        Page<OpeningApplicant> applicants = openingApplicantRepository.findAll(spec, pageable);
        return applicants.map(this::convertToResponse);
    }
}
