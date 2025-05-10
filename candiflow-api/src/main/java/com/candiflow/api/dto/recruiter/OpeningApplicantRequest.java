package com.candiflow.api.dto.recruiter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour la création ou la mise à jour d'un candidat dans le pipeline
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpeningApplicantRequest {

    @NotBlank(message = "Le nom du candidat est obligatoire")
    private String name;

    @Email(message = "Format d'email invalide")
    private String email;

    private String phone;
    
    private String cvStoragePath;
    
    private String coverLetterStoragePath;
    
    private LocalDate applicationDate;
    
    // UUID de l'étape du pipeline (optionnel, "Nouveau" par défaut)
    private UUID pipelineStageId;
    
    // UUID de la source du candidat (optionnel)
    private UUID sourceId;
    
    // Notes initiales (optionnel)
    private String initialNotes;
}
