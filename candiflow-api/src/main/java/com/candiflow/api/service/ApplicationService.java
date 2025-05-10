package com.candiflow.api.service;

import com.candiflow.api.dto.application.ApplicationRequest;
import com.candiflow.api.dto.application.ApplicationResponse;
import com.candiflow.api.dto.application.StatusUpdateRequest;
import com.candiflow.api.model.entity.Application;
import com.candiflow.api.model.entity.ApplicationStatus;
import com.candiflow.api.model.entity.StatusUpdate;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.repository.ApplicationRepository;
import com.candiflow.api.repository.StatusUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StatusUpdateRepository statusUpdateRepository;
    private final ApplicationStatusService applicationStatusService;
    private final AuthService authService;

    /**
     * Crée une nouvelle candidature pour l'utilisateur connecté
     * @param request Données de la candidature
     * @return La candidature créée
     */
    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        User currentUser = authService.getCurrentUser();

        // Créer la candidature
        Application application = new Application();
        application.setUser(currentUser);
        application.setCompanyName(request.getCompanyName());
        application.setJobTitle(request.getJobTitle());
        application.setJobUrl(request.getJobUrl());
        application.setDateApplied(request.getDateApplied());
        application.setFollowUpDate(request.getFollowUpDate());
        application.setGeneralNotes(request.getGeneralNotes());

        // Sauvegarder la candidature
        Application savedApplication = applicationRepository.save(application);

        // Créer le statut initial
        String statusName = request.getInitialStatus();
        if (statusName == null || statusName.isEmpty()) {
            statusName = "Postulé"; // Statut par défaut
        }

        ApplicationStatus status = applicationStatusService.getStatusByName(statusName)
                .orElseGet(applicationStatusService::getDefaultStatus);

        StatusUpdate statusUpdate = new StatusUpdate();
        statusUpdate.setApplication(savedApplication);
        statusUpdate.setStatus(status);
        statusUpdate.setEventDate(request.getDateApplied()); // Même date que la candidature
        statusUpdate.setNotes(request.getInitialStatusNotes());

        StatusUpdate savedStatusUpdate = statusUpdateRepository.save(statusUpdate);

        // Retourner la réponse
        return mapToResponse(savedApplication, savedStatusUpdate);
    }

    /**
     * Met à jour une candidature existante
     * @param id ID de la candidature
     * @param request Nouvelles données
     * @return La candidature mise à jour
     */
    @Transactional
    public ApplicationResponse updateApplication(UUID id, ApplicationRequest request) {
        User currentUser = authService.getCurrentUser();

        // Récupérer la candidature
        Application application = applicationRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Mettre à jour les champs
        application.setCompanyName(request.getCompanyName());
        application.setJobTitle(request.getJobTitle());
        application.setJobUrl(request.getJobUrl());
        application.setDateApplied(request.getDateApplied());
        application.setFollowUpDate(request.getFollowUpDate());
        application.setGeneralNotes(request.getGeneralNotes());

        // Sauvegarder la candidature
        Application updatedApplication = applicationRepository.save(application);

        // Récupérer le dernier statut
        StatusUpdate latestStatus = statusUpdateRepository.findLatestByApplication(updatedApplication)
                .orElse(null);

        // Retourner la réponse
        return mapToResponse(updatedApplication, latestStatus);
    }

    /**
     * Récupère une candidature par son ID
     * @param id ID de la candidature
     * @return La candidature si elle existe
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationResponse> getApplicationById(UUID id) {
        User currentUser = authService.getCurrentUser();

        return applicationRepository.findByIdAndUser(id, currentUser)
                .map(application -> {
                    StatusUpdate latestStatus = statusUpdateRepository.findLatestByApplication(application)
                            .orElse(null);
                    return mapToResponse(application, latestStatus);
                });
    }

    /**
     * Récupère toutes les candidatures de l'utilisateur connecté
     * @param pageable Pagination
     * @return Page de candidatures
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApplications(Pageable pageable) {
        User currentUser = authService.getCurrentUser();

        return applicationRepository.findByUser(currentUser, pageable)
                .map(application -> {
                    StatusUpdate latestStatus = statusUpdateRepository.findLatestByApplication(application)
                            .orElse(null);
                    return mapToResponse(application, latestStatus);
                });
    }

    /**
     * Recherche des candidatures par mot-clé
     * @param keyword Mot-clé de recherche
     * @param pageable Pagination
     * @return Page de candidatures correspondant au mot-clé
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> searchApplications(String keyword, Pageable pageable) {
        User currentUser = authService.getCurrentUser();

        return applicationRepository.searchByUserAndKeyword(currentUser, keyword, pageable)
                .map(application -> {
                    StatusUpdate latestStatus = statusUpdateRepository.findLatestByApplication(application)
                            .orElse(null);
                    return mapToResponse(application, latestStatus);
                });
    }

    /**
     * Supprime une candidature
     * @param id ID de la candidature
     * @return true si la suppression a réussi
     */
    @Transactional
    public boolean deleteApplication(UUID id) {
        User currentUser = authService.getCurrentUser();

        Optional<Application> applicationOpt = applicationRepository.findByIdAndUser(id, currentUser);
        if (applicationOpt.isEmpty()) {
            return false;
        }

        applicationRepository.delete(applicationOpt.get());
        return true;
    }

    /**
     * Ajoute un nouveau statut à une candidature
     * @param applicationId ID de la candidature
     * @param request Données du statut
     * @return La candidature mise à jour
     */
    @Transactional
    public ApplicationResponse addStatusUpdate(UUID applicationId, StatusUpdateRequest request) {
        User currentUser = authService.getCurrentUser();

        // Récupérer la candidature
        Application application = applicationRepository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Récupérer le statut
        ApplicationStatus status = applicationStatusService.getStatusByName(request.getStatusName())
                .orElseThrow(() -> new RuntimeException("Statut non trouvé: " + request.getStatusName()));

        // Créer la mise à jour de statut
        StatusUpdate statusUpdate = new StatusUpdate();
        statusUpdate.setApplication(application);
        statusUpdate.setStatus(status);
        statusUpdate.setEventDate(request.getEventDate());
        statusUpdate.setNotes(request.getNotes());

        // Sauvegarder la mise à jour
        StatusUpdate savedStatusUpdate = statusUpdateRepository.save(statusUpdate);

        // Retourner la candidature mise à jour
        return mapToResponse(application, savedStatusUpdate);
    }

    /**
     * Convertit une entité Application en DTO ApplicationResponse
     */
    private ApplicationResponse mapToResponse(Application application, StatusUpdate latestStatus) {
        ApplicationResponse.StatusUpdateSummary currentStatus = null;
        if (latestStatus != null) {
            currentStatus = ApplicationResponse.StatusUpdateSummary.builder()
                    .id(latestStatus.getId())
                    .statusName(latestStatus.getStatus().getName())
                    .eventDate(latestStatus.getEventDate())
                    .build();
        }

        return ApplicationResponse.builder()
                .id(application.getId())
                .companyName(application.getCompanyName())
                .jobTitle(application.getJobTitle())
                .jobUrl(application.getJobUrl())
                .dateApplied(application.getDateApplied())
                .followUpDate(application.getFollowUpDate())
                .generalNotes(application.getGeneralNotes())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .currentStatus(currentStatus)
                .totalStatusUpdates((int) statusUpdateRepository.countByApplication(application))
                .totalDocuments(application.getDocuments().size())
                .build();
    }
}
