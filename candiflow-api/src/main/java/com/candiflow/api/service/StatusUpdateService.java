package com.candiflow.api.service;

import com.candiflow.api.dto.application.StatusUpdateRequest;
import com.candiflow.api.dto.application.StatusUpdateResponse;
import com.candiflow.api.model.entity.Application;
import com.candiflow.api.model.entity.ApplicationStatus;
import com.candiflow.api.model.entity.StatusUpdate;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.repository.ApplicationRepository;
import com.candiflow.api.repository.StatusUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatusUpdateService {

    private final StatusUpdateRepository statusUpdateRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusService applicationStatusService;
    private final AuthService authService;

    /**
     * Récupère toutes les mises à jour de statut pour une candidature
     * @param applicationId ID de la candidature
     * @return Liste des mises à jour de statut
     */
    @Transactional(readOnly = true)
    public List<StatusUpdateResponse> getStatusUpdatesForApplication(UUID applicationId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // Récupérer la candidature
        Application application = applicationRepository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Récupérer les mises à jour de statut
        return statusUpdateRepository.findByApplicationOrderByEventDateDesc(application).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une mise à jour de statut par son ID
     * @param applicationId ID de la candidature
     * @param statusUpdateId ID de la mise à jour de statut
     * @return La mise à jour de statut si elle existe
     */
    @Transactional(readOnly = true)
    public Optional<StatusUpdateResponse> getStatusUpdateById(UUID applicationId, UUID statusUpdateId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // Récupérer la candidature
        Application application = applicationRepository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Récupérer la mise à jour de statut
        return statusUpdateRepository.findByIdAndApplication(statusUpdateId, application)
                .map(this::mapToResponse);
    }

    /**
     * Crée une nouvelle mise à jour de statut pour une candidature
     * @param applicationId ID de la candidature
     * @param request Données de la mise à jour de statut
     * @return La mise à jour de statut créée
     */
    @Transactional
    public StatusUpdateResponse createStatusUpdate(UUID applicationId, StatusUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

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

        // Retourner la réponse
        return mapToResponse(savedStatusUpdate);
    }

    /**
     * Met à jour une mise à jour de statut existante
     * @param applicationId ID de la candidature
     * @param statusUpdateId ID de la mise à jour de statut
     * @param request Nouvelles données
     * @return La mise à jour de statut modifiée
     */
    @Transactional
    public StatusUpdateResponse updateStatusUpdate(UUID applicationId, UUID statusUpdateId, StatusUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // Récupérer la candidature
        Application application = applicationRepository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Récupérer la mise à jour de statut
        StatusUpdate statusUpdate = statusUpdateRepository.findByIdAndApplication(statusUpdateId, application)
                .orElseThrow(() -> new RuntimeException("Mise à jour de statut non trouvée"));

        // Récupérer le statut
        ApplicationStatus status = applicationStatusService.getStatusByName(request.getStatusName())
                .orElseThrow(() -> new RuntimeException("Statut non trouvé: " + request.getStatusName()));

        // Mettre à jour les champs
        statusUpdate.setStatus(status);
        statusUpdate.setEventDate(request.getEventDate());
        statusUpdate.setNotes(request.getNotes());

        // Sauvegarder la mise à jour
        StatusUpdate updatedStatusUpdate = statusUpdateRepository.save(statusUpdate);

        // Retourner la réponse
        return mapToResponse(updatedStatusUpdate);
    }

    /**
     * Supprime une mise à jour de statut
     * @param applicationId ID de la candidature
     * @param statusUpdateId ID de la mise à jour de statut
     * @return true si la suppression a réussi
     */
    @Transactional
    public boolean deleteStatusUpdate(UUID applicationId, UUID statusUpdateId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // Récupérer la candidature
        Application application = applicationRepository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Récupérer la mise à jour de statut
        Optional<StatusUpdate> statusUpdateOpt = statusUpdateRepository.findByIdAndApplication(statusUpdateId, application);
        if (statusUpdateOpt.isEmpty()) {
            return false;
        }

        // Vérifier qu'il reste au moins une mise à jour de statut après suppression
        long count = statusUpdateRepository.countByApplication(application);
        if (count <= 1) {
            throw new RuntimeException("Impossible de supprimer la dernière mise à jour de statut");
        }

        // Supprimer la mise à jour
        statusUpdateRepository.delete(statusUpdateOpt.get());
        return true;
    }

    /**
     * Convertit une entité StatusUpdate en DTO StatusUpdateResponse
     */
    private StatusUpdateResponse mapToResponse(StatusUpdate statusUpdate) {
        return StatusUpdateResponse.builder()
                .id(statusUpdate.getId())
                .applicationId(statusUpdate.getApplication().getId())
                .statusId(statusUpdate.getStatus().getId())
                .statusName(statusUpdate.getStatus().getName())
                .statusDescription(statusUpdate.getStatus().getDescription())
                .statusIconName(statusUpdate.getStatus().getIconName())
                .eventDate(statusUpdate.getEventDate())
                .notes(statusUpdate.getNotes())
                .createdAt(statusUpdate.getCreatedAt())
                .updatedAt(statusUpdate.getUpdatedAt())
                .build();
    }
}
