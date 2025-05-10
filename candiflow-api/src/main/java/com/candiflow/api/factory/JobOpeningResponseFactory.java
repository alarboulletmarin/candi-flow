package com.candiflow.api.factory;

import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.repository.OpeningApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory pour créer des objets JobOpeningResponse à partir d'entités JobOpening
 * Implémente le pattern Factory pour centraliser la logique de création
 */
@Component
@RequiredArgsConstructor
public class JobOpeningResponseFactory {

    private final OpeningApplicantRepository openingApplicantRepository;

    /**
     * Crée un objet JobOpeningResponse à partir d'une entité JobOpening
     * @param jobOpening L'entité JobOpening source
     * @return Un objet JobOpeningResponse
     */
    public JobOpeningResponse createFromEntity(JobOpening jobOpening) {
        JobOpeningResponse response = new JobOpeningResponse();
        response.setId(jobOpening.getId());
        response.setTitle(jobOpening.getTitle());
        response.setDescription(jobOpening.getDescription());
        response.setStatus(jobOpening.getStatus());
        response.setCreatedAt(jobOpening.getCreatedAt());
        response.setUpdatedAt(jobOpening.getUpdatedAt());
        
        // Ajouter les statistiques
        long totalApplicants = openingApplicantRepository.countByJobOpening(jobOpening);
        response.setTotalApplicants((int) totalApplicants);
        
        return response;
    }
}
