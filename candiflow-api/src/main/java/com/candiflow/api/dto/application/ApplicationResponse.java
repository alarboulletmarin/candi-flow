package com.candiflow.api.dto.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'une candidature
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResponse {

    private UUID id;
    private String companyName;
    private String jobTitle;
    private String jobUrl;
    private Instant dateApplied;
    private LocalDate followUpDate;
    private String generalNotes;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Informations sur le statut actuel
    private StatusUpdateSummary currentStatus;
    
    // Statistiques
    private int totalStatusUpdates;
    private int totalDocuments;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusUpdateSummary {
        private UUID id;
        private String statusName;
        private Instant eventDate;
    }
}
