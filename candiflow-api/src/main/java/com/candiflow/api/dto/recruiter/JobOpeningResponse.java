package com.candiflow.api.dto.recruiter;

import com.candiflow.api.model.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'une offre d'emploi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobOpeningResponse {

    private UUID id;
    private String title;
    private String description;
    private JobStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Statistiques
    private int totalApplicants;
    private int newApplicants;
    private int inProcessApplicants;
    private int endStageApplicants;
}
