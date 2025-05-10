package com.candiflow.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le nombre d'offres d'emploi par statut
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusCount {
    private String status;
    private long count;
}
