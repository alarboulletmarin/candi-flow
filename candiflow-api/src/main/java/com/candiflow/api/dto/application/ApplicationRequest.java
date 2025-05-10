package com.candiflow.api.dto.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO pour la création ou la mise à jour d'une candidature
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationRequest {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    private String companyName;

    @NotBlank(message = "Le titre du poste est obligatoire")
    private String jobTitle;

    private String jobUrl;

    @NotNull(message = "La date de candidature est obligatoire")
    private Instant dateApplied;

    private LocalDate followUpDate;

    private String generalNotes;

    // Statut initial (optionnel, "Postulé" par défaut)
    private String initialStatus;
    
    // Notes pour le statut initial (optionnel)
    private String initialStatusNotes;
}
