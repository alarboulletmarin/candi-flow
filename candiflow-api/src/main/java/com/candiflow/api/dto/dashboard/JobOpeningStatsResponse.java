package com.candiflow.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les statistiques d'une offre d'emploi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOpeningStatsResponse {
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private long totalApplicants;
    private List<StageCount> applicantsByStage;
    private List<SourceCount> applicantsBySource;
    private List<DateCount> weeklyApplicationTrend;
}
