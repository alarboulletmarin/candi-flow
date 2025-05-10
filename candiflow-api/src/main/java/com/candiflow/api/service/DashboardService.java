package com.candiflow.api.service;

import com.candiflow.api.dto.dashboard.*;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.PipelineStage;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour les tableaux de bord et statistiques
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobOpeningRepository jobOpeningRepository;
    private final OpeningApplicantRepository openingApplicantRepository;
    private final PipelineStageRepository pipelineStageRepository;

    /**
     * Récupère les statistiques globales pour un recruteur
     * @param recruiter Recruteur
     * @return Statistiques globales
     */
    @Transactional(readOnly = true)
    public RecruiterDashboardResponse getRecruiterDashboard(User recruiter) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays((long)LocalDate.now().getDayOfWeek().getValue() - 1).atStartOfDay();

        // Statistiques des offres d'emploi
        List<JobOpening> allJobOpenings = jobOpeningRepository.findByRecruiterOrderByCreatedAtDesc(recruiter);
        long activeJobOpenings = allJobOpenings.stream()
                .filter(jo -> JobStatus.OPEN == jo.getStatus())
                .count();
        
        // Statistiques des candidats
        List<OpeningApplicant> allApplicants = new ArrayList<>();
        for (JobOpening jobOpening : allJobOpenings) {
            allApplicants.addAll(openingApplicantRepository.findByJobOpening(jobOpening));
        }
        
        long newApplicantsThisMonth = allApplicants.stream()
                .filter(a -> a.getApplicationDate() != null && a.getApplicationDate().atStartOfDay().isAfter(startOfMonth))
                .count();
        
        long newApplicantsThisWeek = allApplicants.stream()
                .filter(a -> a.getApplicationDate() != null && a.getApplicationDate().atStartOfDay().isAfter(startOfWeek))
                .count();
        
        // Temps moyen de recrutement (en jours)
        OptionalDouble avgHiringTime = allApplicants.stream()
                .filter(a -> "HIRED".equals(a.getCurrentStage().getName()))
                .mapToLong(a -> ChronoUnit.DAYS.between(a.getApplicationDate().atStartOfDay(), a.getUpdatedAt()))
                .average();
        
        // Taux de conversion par étape
        Map<String, Long> applicantsByStage = allApplicants.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCurrentStage().getName(),
                        Collectors.counting()
                ));
        
        List<StageConversionRate> conversionRates = new ArrayList<>();
        // Récupérer toutes les étapes et les trier par ordre
        List<PipelineStage> allStages = pipelineStageRepository.findAll();
        List<PipelineStage> orderedStages = allStages.stream()
                .sorted(Comparator.comparing(PipelineStage::getDisplayOrder))
                .collect(Collectors.toList());
        
        for (int i = 0; i < orderedStages.size() - 1; i++) {
            PipelineStage currentStage = orderedStages.get(i);
            PipelineStage nextStage = orderedStages.get(i + 1);
            
            Long currentCount = applicantsByStage.getOrDefault(currentStage.getName(), 0L);
            Long nextCount = applicantsByStage.getOrDefault(nextStage.getName(), 0L);
            
            double rate = currentCount > 0 ? (double) nextCount / currentCount * 100 : 0;
            
            conversionRates.add(new StageConversionRate(
                    currentStage.getName(),
                    nextStage.getName(),
                    rate
            ));
        }
        
        // Utiliser le pattern Builder pour construire la réponse
        return RecruiterDashboardResponse.builder()
                .totalJobOpenings(allJobOpenings.size())
                .activeJobOpenings(activeJobOpenings)
                .totalApplicants(allApplicants.size())
                .newApplicantsThisMonth(newApplicantsThisMonth)
                .newApplicantsThisWeek(newApplicantsThisWeek)
                .averageHiringTimeInDays(avgHiringTime.orElse(0))
                .conversionRates(conversionRates)
                .build();
    }
    
    /**
     * Récupère les statistiques pour une offre d'emploi spécifique
     * @param jobOpeningId ID de l'offre d'emploi
     * @return Statistiques de l'offre d'emploi
     */
    @Transactional(readOnly = true)
    public JobOpeningStatsResponse getJobOpeningStats(UUID jobOpeningId) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new NoSuchElementException("Offre d'emploi non trouvée"));
        
        List<OpeningApplicant> applicants = openingApplicantRepository.findByJobOpening(jobOpening);
        
        // Nombre de candidats par étape
        Map<String, Long> applicantsByStage = applicants.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCurrentStage().getName(),
                        Collectors.counting()
                ));
        
        List<StageCount> stageCounts = applicantsByStage.entrySet().stream()
                .map(entry -> new StageCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        // Nombre de candidats par source
        Map<String, Long> applicantsBySource = applicants.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getSource().getName(),
                        Collectors.counting()
                ));
        
        List<SourceCount> sourceCounts = applicantsBySource.entrySet().stream()
                .map(entry -> new SourceCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        // Tendance des candidatures (par semaine)
        Map<LocalDate, Long> applicantsByWeek = applicants.stream()
                .filter(a -> a.getApplicationDate() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getApplicationDate().minusDays((long)a.getApplicationDate().getDayOfWeek().getValue() - 1),
                        Collectors.counting()
                ));
        
        List<DateCount> weeklyTrend = applicantsByWeek.entrySet().stream()
                .map(entry -> new DateCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DateCount::getDate))
                .collect(Collectors.toList());
        
        JobOpeningStatsResponse response = new JobOpeningStatsResponse();
        response.setTitle(jobOpening.getTitle());
        response.setStatus(jobOpening.getStatus().toString());
        response.setCreatedAt(jobOpening.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        response.setTotalApplicants(applicants.size());
        response.setApplicantsByStage(stageCounts);
        response.setApplicantsBySource(sourceCounts);
        response.setWeeklyApplicationTrend(weeklyTrend);
        return response;
    }
    
    /**
     * Récupère les statistiques globales pour tous les recruteurs (admin)
     * @return Statistiques globales
     */
    @Transactional(readOnly = true)
    public GlobalStatsResponse getGlobalStats() {
        List<JobOpening> allJobOpenings = jobOpeningRepository.findAll();
        List<OpeningApplicant> allApplicants = openingApplicantRepository.findAll();
        
        // Nombre d'offres par statut
        Map<String, Long> jobOpeningsByStatus = allJobOpenings.stream()
                .collect(Collectors.groupingBy(
                        jo -> jo.getStatus().toString(),
                        Collectors.counting()
                ));
        
        List<StatusCount> statusCounts = jobOpeningsByStatus.entrySet().stream()
                .map(entry -> new StatusCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        // Nombre de candidats par étape
        Map<String, Long> applicantsByStage = allApplicants.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCurrentStage().getName(),
                        Collectors.counting()
                ));
        
        List<StageCount> stageCounts = applicantsByStage.entrySet().stream()
                .map(entry -> new StageCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        // Tendance des offres (par mois)
        Map<LocalDate, Long> jobOpeningsByMonth = allJobOpenings.stream()
                .collect(Collectors.groupingBy(
                        jo -> jo.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1),
                        Collectors.counting()
                ));
        
        List<DateCount> monthlyTrend = jobOpeningsByMonth.entrySet().stream()
                .map(entry -> new DateCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DateCount::getDate))
                .collect(Collectors.toList());
        
        return new GlobalStatsResponse(
                allJobOpenings.size(),
                allApplicants.size(),
                statusCounts,
                stageCounts,
                monthlyTrend
        );
    }
}
