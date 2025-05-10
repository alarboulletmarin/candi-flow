package com.candiflow.api.dto.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'une mise à jour de statut
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateResponse {

    private UUID id;
    private UUID applicationId;
    private UUID statusId;
    private String statusName;
    private String statusDescription;
    private String statusIconName;
    private Instant eventDate;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
