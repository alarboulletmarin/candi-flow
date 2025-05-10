package com.candiflow.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le nombre de candidats par étape
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageCount {
    private String stageName;
    private long count;
}
