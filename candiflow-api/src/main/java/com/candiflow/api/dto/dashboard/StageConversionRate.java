package com.candiflow.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le taux de conversion entre deux étapes du pipeline
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageConversionRate {
    private String fromStage;
    private String toStage;
    private double conversionRate;
}
