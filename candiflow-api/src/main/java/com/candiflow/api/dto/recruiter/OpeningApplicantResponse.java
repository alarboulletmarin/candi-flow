package com.candiflow.api.dto.recruiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'un candidat dans le pipeline
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpeningApplicantResponse {

    private UUID id;
    private UUID jobOpeningId;
    private String name;
    private String email;
    private String phone;
    private String cvStoragePath;
    private String coverLetterStoragePath;
    private LocalDate applicationDate;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Informations sur l'étape du pipeline
    private PipelineStageSummary pipelineStage;
    
    // Informations sur la source du candidat
    private CandidateSourceSummary source;
    
    // Statistiques
    private int totalNotes;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PipelineStageSummary {
        private UUID id;
        private String name;
        private boolean isEndStage;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CandidateSourceSummary {
        private UUID id;
        private String name;
    }
}
