package com.candiflow.api.factory;

import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.model.entity.OpeningApplicant;
import org.springframework.stereotype.Component;

/**
 * Factory pour créer des objets OpeningApplicantResponse à partir d'entités OpeningApplicant
 * Implémente le pattern Factory pour centraliser la logique de création
 */
@Component
public class OpeningApplicantResponseFactory {

    /**
     * Crée un objet OpeningApplicantResponse à partir d'une entité OpeningApplicant
     * @param applicant L'entité OpeningApplicant source
     * @return Un objet OpeningApplicantResponse
     */
    public OpeningApplicantResponse createFromEntity(OpeningApplicant applicant) {
        OpeningApplicantResponse response = new OpeningApplicantResponse();
        response.setId(applicant.getId());
        response.setName(applicant.getName());
        response.setEmail(applicant.getEmail());
        response.setPhone(applicant.getPhone());
        response.setApplicationDate(applicant.getApplicationDate());
        response.setCreatedAt(applicant.getCreatedAt());
        response.setUpdatedAt(applicant.getUpdatedAt());
        
        // Ajouter les relations
        if (applicant.getJobOpening() != null) {
            response.setJobOpeningId(applicant.getJobOpening().getId());
        }
        
        if (applicant.getCurrentStage() != null) {
            OpeningApplicantResponse.PipelineStageSummary stageSummary = new OpeningApplicantResponse.PipelineStageSummary();
            stageSummary.setId(applicant.getCurrentStage().getId());
            stageSummary.setName(applicant.getCurrentStage().getName());
            stageSummary.setEndStage(false); // Par défaut, on suppose que ce n'est pas une étape finale
            response.setPipelineStage(stageSummary);
        }
        
        if (applicant.getSource() != null) {
            OpeningApplicantResponse.CandidateSourceSummary sourceSummary = new OpeningApplicantResponse.CandidateSourceSummary();
            sourceSummary.setId(applicant.getSource().getId());
            sourceSummary.setName(applicant.getSource().getName());
            response.setSource(sourceSummary);
        }
        
        return response;
    }
}
