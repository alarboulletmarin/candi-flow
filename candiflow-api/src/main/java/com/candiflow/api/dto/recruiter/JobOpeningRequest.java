package com.candiflow.api.dto.recruiter;

import com.candiflow.api.model.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création ou la mise à jour d'une offre d'emploi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobOpeningRequest {

    @NotBlank(message = "Le titre de l'offre est obligatoire")
    private String title;

    private String description;

    @NotNull(message = "Le statut de l'offre est obligatoire")
    private JobStatus status;
}
