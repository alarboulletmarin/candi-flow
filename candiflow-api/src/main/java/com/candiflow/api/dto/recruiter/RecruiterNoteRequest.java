package com.candiflow.api.dto.recruiter;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création ou la mise à jour d'une note de recruteur
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterNoteRequest {

    @NotBlank(message = "Le contenu de la note est obligatoire")
    private String noteText;
}
