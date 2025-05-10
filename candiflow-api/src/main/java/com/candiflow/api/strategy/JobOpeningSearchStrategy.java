package com.candiflow.api.strategy;

import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.factory.JobOpeningResponseFactory;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stratégie de recherche pour les offres d'emploi
 * Implémente le pattern Strategy
 */
@Data
@Builder
public class JobOpeningSearchStrategy implements SearchStrategy<JobOpening, JobOpeningResponse> {
    
    private final JobOpeningRepository jobOpeningRepository;
    private final OpeningApplicantRepository openingApplicantRepository;
    private final JobOpeningResponseFactory jobOpeningResponseFactory;
    
    // Critères de recherche
    private String title;
    private String status;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    private UUID recruiterId;
    
    @Override
    public Specification<JobOpening> createSpecification() {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtre par titre (recherche partielle, insensible à la casse)
            if (title != null && !title.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"));
            }
            
            // Filtre par statut
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status").as(String.class), status));
            }
            
            // Filtre par recruteur
            if (recruiterId != null) {
                predicates.add(criteriaBuilder.equal(root.get("recruiter").get("id"), recruiterId));
            }
            
            // Filtre par date de création (après)
            if (createdAfter != null) {
                LocalDateTime startOfDay = createdAfter.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), startOfDay.atZone(java.time.ZoneId.systemDefault()).toInstant()));
            }
            
            // Filtre par date de création (avant)
            if (createdBefore != null) {
                LocalDateTime endOfDay = createdBefore.plusDays(1).atStartOfDay();
                predicates.add(criteriaBuilder.lessThan(
                        root.get("createdAt"), endOfDay.atZone(java.time.ZoneId.systemDefault()).toInstant()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    @Override
    public JobOpeningResponse convertToResponse(JobOpening entity) {
        return jobOpeningResponseFactory.createFromEntity(entity);
    }
    
    @Override
    public Page<JobOpeningResponse> execute(Pageable pageable) {
        Specification<JobOpening> spec = createSpecification();
        Page<JobOpening> jobOpenings = jobOpeningRepository.findAll(spec, pageable);
        return jobOpenings.map(this::convertToResponse);
    }
}
