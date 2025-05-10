package com.candiflow.api.service;

import com.candiflow.api.dto.application.ApplicationStatusResponse;
import com.candiflow.api.model.entity.ApplicationStatus;
import com.candiflow.api.repository.ApplicationStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationStatusService {

    private final ApplicationStatusRepository applicationStatusRepository;

    /**
     * Récupère tous les statuts actifs
     * @return Liste des statuts actifs
     */
    @Transactional(readOnly = true)
    public List<ApplicationStatusResponse> getAllActiveStatuses() {
        return applicationStatusRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un statut par son ID
     * @param id ID du statut
     * @return Le statut s'il existe
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationStatusResponse> getStatusById(UUID id) {
        return applicationStatusRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Récupère un statut par son nom
     * @param name Nom du statut
     * @return Le statut s'il existe
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationStatus> getStatusByName(String name) {
        return applicationStatusRepository.findByName(name);
    }

    /**
     * Récupère le statut par défaut pour les nouvelles candidatures
     * @return Le statut par défaut
     */
    @Transactional(readOnly = true)
    public ApplicationStatus getDefaultStatus() {
        return applicationStatusRepository.findDefaultStatus()
                .orElseGet(() -> {
                    // Si le statut par défaut n'existe pas, on le crée
                    ApplicationStatus defaultStatus = new ApplicationStatus();
                    defaultStatus.setName("Postulé");
                    defaultStatus.setDescription("Candidature soumise");
                    defaultStatus.setDisplayOrder(1);
                    defaultStatus.setIconName("send");
                    defaultStatus.setActive(true);
                    return applicationStatusRepository.save(defaultStatus);
                });
    }

    /**
     * Initialise les statuts par défaut si la table est vide
     */
    @Transactional
    public void initializeDefaultStatuses() {
        if (applicationStatusRepository.count() == 0) {
            // Créer les statuts par défaut
            createDefaultStatus("Postulé", "Candidature soumise", 1, "send");
            createDefaultStatus("CV Sélectionné", "CV retenu pour la suite du processus", 2, "check_circle");
            createDefaultStatus("Entretien RH", "Entretien avec les ressources humaines", 3, "people");
            createDefaultStatus("Test Technique", "Évaluation technique", 4, "code");
            createDefaultStatus("Entretien Technique", "Entretien avec l'équipe technique", 5, "computer");
            createDefaultStatus("Entretien Final", "Dernier entretien avant décision", 6, "group");
            createDefaultStatus("Offre Reçue", "Proposition d'embauche reçue", 7, "local_offer");
            createDefaultStatus("Négociation", "Discussion des conditions", 8, "handshake");
            createDefaultStatus("Accepté", "Offre acceptée", 9, "thumb_up");
            createDefaultStatus("Refusé", "Candidature non retenue", 10, "thumb_down");
            createDefaultStatus("Abandonné", "Candidature abandonnée par le candidat", 11, "cancel");
        }
    }

    /**
     * Crée un statut par défaut
     */
    private ApplicationStatus createDefaultStatus(String name, String description, int order, String icon) {
        ApplicationStatus status = new ApplicationStatus();
        status.setName(name);
        status.setDescription(description);
        status.setDisplayOrder(order);
        status.setIconName(icon);
        status.setActive(true);
        return applicationStatusRepository.save(status);
    }

    /**
     * Convertit une entité ApplicationStatus en DTO ApplicationStatusResponse
     */
    private ApplicationStatusResponse mapToResponse(ApplicationStatus status) {
        return ApplicationStatusResponse.builder()
                .id(status.getId())
                .name(status.getName())
                .description(status.getDescription())
                .displayOrder(status.getDisplayOrder())
                .iconName(status.getIconName())
                .isActive(status.isActive())
                .build();
    }
}
