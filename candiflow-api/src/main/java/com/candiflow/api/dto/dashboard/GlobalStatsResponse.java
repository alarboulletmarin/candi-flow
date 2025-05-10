package com.candiflow.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour les statistiques globales (admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatsResponse {
    private long totalJobOpenings;
    private long totalApplicants;
    private List<StatusCount> jobOpeningsByStatus;
    private List<StageCount> applicantsByStage;
    private List<DateCount> monthlyJobOpeningTrend;
}
