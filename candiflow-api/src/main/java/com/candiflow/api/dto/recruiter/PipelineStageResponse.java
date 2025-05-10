package com.candiflow.api.dto.recruiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'une étape du pipeline
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PipelineStageResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer displayOrder;
    private boolean isEndStage;
    private boolean isActive;
}
