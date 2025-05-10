package com.candiflow.api.service;

import com.candiflow.api.dto.recruiter.PipelineStageResponse;
import com.candiflow.api.model.entity.PipelineStage;
import com.candiflow.api.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PipelineStageService {

    private final PipelineStageRepository pipelineStageRepository;

    /**
     * Récupère toutes les étapes actives du pipeline
     * @return Liste des étapes actives
     */
    @Transactional(readOnly = true)
    public List<PipelineStageResponse> getAllActiveStages() {
        return pipelineStageRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une étape par son ID
     * @param id ID de l'étape
     * @return L'étape si elle existe
     */
    @Transactional(readOnly = true)
    public Optional<PipelineStageResponse> getStageById(UUID id) {
        return pipelineStageRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Récupère une étape par son nom
     * @param name Nom de l'étape
     * @return L'étape si elle existe
     */
    @Transactional(readOnly = true)
    public Optional<PipelineStage> getStageByName(String name) {
        return pipelineStageRepository.findByName(name);
    }

    /**
     * Récupère l'étape par défaut pour les nouveaux candidats
     * @return L'étape par défaut
     */
    @Transactional(readOnly = true)
    public PipelineStage getDefaultStage() {
        return pipelineStageRepository.findDefaultStage()
                .orElseGet(() -> {
                    // Si l'étape par défaut n'existe pas, on la crée
                    PipelineStage defaultStage = new PipelineStage();
                    defaultStage.setName("Nouveau");
                    defaultStage.setDescription("Nouveau candidat ajouté au pipeline");
                    defaultStage.setDisplayOrder(1);
                    defaultStage.setEndStage(false);
                    defaultStage.setActive(true);
                    return pipelineStageRepository.save(defaultStage);
                });
    }

    /**
     * Initialise les étapes par défaut si la table est vide
     */
    @Transactional
    public void initializeDefaultStages() {
        if (pipelineStageRepository.count() == 0) {
            // Créer les étapes par défaut
            createDefaultStage("Nouveau", "Nouveau candidat ajouté au pipeline", 1, false);
            createDefaultStage("Présélection", "CV/Profil en cours d'évaluation", 2, false);
            createDefaultStage("Entretien RH", "Entretien avec les ressources humaines", 3, false);
            createDefaultStage("Test Technique", "Évaluation technique en cours", 4, false);
            createDefaultStage("Entretien Technique", "Entretien avec l'équipe technique", 5, false);
            createDefaultStage("Entretien Final", "Dernier entretien avant décision", 6, false);
            createDefaultStage("Offre", "Proposition d'embauche en cours", 7, false);
            createDefaultStage("Recruté", "Candidat recruté", 8, true);
            createDefaultStage("Refusé", "Candidature non retenue", 9, true);
            createDefaultStage("Abandonné", "Candidat a abandonné le processus", 10, true);
        }
    }

    /**
     * Crée une étape par défaut
     */
    private PipelineStage createDefaultStage(String name, String description, int order, boolean isEndStage) {
        PipelineStage stage = new PipelineStage();
        stage.setName(name);
        stage.setDescription(description);
        stage.setDisplayOrder(order);
        stage.setEndStage(isEndStage);
        stage.setActive(true);
        return pipelineStageRepository.save(stage);
    }

    /**
     * Convertit une entité PipelineStage en DTO PipelineStageResponse
     */
    private PipelineStageResponse mapToResponse(PipelineStage stage) {
        return PipelineStageResponse.builder()
                .id(stage.getId())
                .name(stage.getName())
                .description(stage.getDescription())
                .displayOrder(stage.getDisplayOrder())
                .isEndStage(stage.isEndStage())
                .isActive(stage.isActive())
                .build();
    }
}
