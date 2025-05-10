package com.candiflow.api.service;

import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.factory.JobOpeningResponseFactory;
import com.candiflow.api.factory.OpeningApplicantResponseFactory;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.strategy.JobOpeningSearchStrategy;
import com.candiflow.api.strategy.OpeningApplicantSearchStrategy;
import com.candiflow.api.strategy.SearchStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service pour la recherche avancée
 * Utilise le pattern Strategy pour gérer différentes stratégies de recherche
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final JobOpeningRepository jobOpeningRepository;
    private final OpeningApplicantRepository openingApplicantRepository;
    private final JobOpeningResponseFactory jobOpeningResponseFactory;
    private final OpeningApplicantResponseFactory openingApplicantResponseFactory;

    /**
     * Recherche avancée d'offres d'emploi
     * @param title Titre de l'offre (optionnel)
     * @param status Statut de l'offre (optionnel)
     * @param createdAfter Date de création minimum (optionnel)
     * @param createdBefore Date de création maximum (optionnel)
     * @param recruiterId ID du recruteur (optionnel)
     * @param pageable Pagination
     * @return Page d'offres d'emploi correspondant aux critères
     */
    @Transactional(readOnly = true)
    public Page<JobOpeningResponse> searchJobOpenings(
            String title,
            String status,
            LocalDate createdAfter,
            LocalDate createdBefore,
            UUID recruiterId,
            Pageable pageable) {

        // Créer une stratégie de recherche pour les offres d'emploi
        SearchStrategy<?, JobOpeningResponse> strategy = JobOpeningSearchStrategy.builder()
                .jobOpeningRepository(jobOpeningRepository)
                .openingApplicantRepository(openingApplicantRepository)
                .jobOpeningResponseFactory(jobOpeningResponseFactory)
                .title(title)
                .status(status)
                .createdAfter(createdAfter)
                .createdBefore(createdBefore)
                .recruiterId(recruiterId)
                .build();
        
        // Exécuter la recherche
        return strategy.execute(pageable);
    }

    /**
     * Recherche avancée de candidats
     * @param jobOpeningId ID de l'offre d'emploi (optionnel)
     * @param name Nom du candidat (optionnel)
     * @param email Email du candidat (optionnel)
     * @param phone Téléphone du candidat (optionnel)
     * @param stageId ID de l'étape du pipeline (optionnel)
     * @param sourceId ID de la source du candidat (optionnel)
     * @param applicationDateAfter Date de candidature minimum (optionnel)
     * @param applicationDateBefore Date de candidature maximum (optionnel)
     * @param pageable Pagination
     * @return Page de candidats correspondant aux critères
     */
    @Transactional(readOnly = true)
    public Page<OpeningApplicantResponse> searchApplicants(
            UUID jobOpeningId,
            String name,
            String email,
            String phone,
            UUID stageId,
            UUID sourceId,
            LocalDate applicationDateAfter,
            LocalDate applicationDateBefore,
            Pageable pageable) {

        // Créer une stratégie de recherche pour les candidats
        SearchStrategy<?, OpeningApplicantResponse> strategy = OpeningApplicantSearchStrategy.builder()
                .openingApplicantRepository(openingApplicantRepository)
                .openingApplicantResponseFactory(openingApplicantResponseFactory)
                .jobOpeningId(jobOpeningId)
                .name(name)
                .email(email)
                .phone(phone)
                .stageId(stageId)
                .sourceId(sourceId)
                .applicationDateAfter(applicationDateAfter)
                .applicationDateBefore(applicationDateBefore)
                .build();
        
        // Exécuter la recherche
        return strategy.execute(pageable);
    }
}
