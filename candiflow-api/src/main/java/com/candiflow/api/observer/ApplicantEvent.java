package com.candiflow.api.observer;

import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.PipelineStage;
import lombok.Getter;

/**
 * Événement lié aux candidats
 * Implémente le pattern Observer
 */
@Getter
public class ApplicantEvent extends BaseEvent {
    
    private final OpeningApplicant applicant;
    private final PipelineStage previousStage;
    
    /**
     * Constructeur
     * @param type Type d'événement
     * @param source Source de l'événement
     * @param applicant Candidat concerné
     * @param previousStage Étape précédente (pour les changements d'étape)
     */
    public ApplicantEvent(EventType type, String source, OpeningApplicant applicant, PipelineStage previousStage) {
        super(type, source);
        this.applicant = applicant;
        this.previousStage = previousStage;
    }
    
    /**
     * Crée un événement de création de candidat
     * @param source Source de l'événement
     * @param applicant Candidat créé
     * @return Événement de création de candidat
     */
    public static ApplicantEvent created(String source, OpeningApplicant applicant) {
        return new ApplicantEvent(EventType.APPLICANT_CREATED, source, applicant, null);
    }
    
    /**
     * Crée un événement de mise à jour de candidat
     * @param source Source de l'événement
     * @param applicant Candidat mis à jour
     * @return Événement de mise à jour de candidat
     */
    public static ApplicantEvent updated(String source, OpeningApplicant applicant) {
        return new ApplicantEvent(EventType.APPLICANT_UPDATED, source, applicant, null);
    }
    
    /**
     * Crée un événement de suppression de candidat
     * @param source Source de l'événement
     * @param applicant Candidat supprimé
     * @return Événement de suppression de candidat
     */
    public static ApplicantEvent deleted(String source, OpeningApplicant applicant) {
        return new ApplicantEvent(EventType.APPLICANT_DELETED, source, applicant, null);
    }
    
    /**
     * Crée un événement de changement d'étape
     * @param source Source de l'événement
     * @param applicant Candidat concerné
     * @param previousStage Étape précédente
     * @return Événement de changement d'étape
     */
    public static ApplicantEvent stageChanged(String source, OpeningApplicant applicant, PipelineStage previousStage) {
        return new ApplicantEvent(EventType.STAGE_CHANGED, source, applicant, previousStage);
    }
}
