package com.candiflow.api.dto.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'un statut de candidature
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer displayOrder;
    private String iconName;
    private boolean isActive;
}
