package com.candiflow.api.dto.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO pour la création ou la mise à jour d'un statut de candidature
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequest {

    @NotBlank(message = "Le nom du statut est obligatoire")
    private String statusName;

    @NotNull(message = "La date de l'événement est obligatoire")
    private Instant eventDate;

    private String notes;
}
