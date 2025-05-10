package com.candiflow.api.observer;

import com.candiflow.api.model.entity.JobOpening;
import lombok.Getter;

/**
 * Événement lié aux offres d'emploi
 * Implémente le pattern Observer
 */
@Getter
public class JobOpeningEvent extends BaseEvent {
    
    private final JobOpening jobOpening;
    
    /**
     * Constructeur
     * @param type Type d'événement
     * @param source Source de l'événement
     * @param jobOpening Offre d'emploi concernée
     */
    public JobOpeningEvent(EventType type, String source, JobOpening jobOpening) {
        super(type, source);
        this.jobOpening = jobOpening;
    }
    
    /**
     * Crée un événement de création d'offre d'emploi
     * @param source Source de l'événement
     * @param jobOpening Offre d'emploi créée
     * @return Événement de création d'offre d'emploi
     */
    public static JobOpeningEvent created(String source, JobOpening jobOpening) {
        return new JobOpeningEvent(EventType.JOB_OPENING_CREATED, source, jobOpening);
    }
    
    /**
     * Crée un événement de mise à jour d'offre d'emploi
     * @param source Source de l'événement
     * @param jobOpening Offre d'emploi mise à jour
     * @return Événement de mise à jour d'offre d'emploi
     */
    public static JobOpeningEvent updated(String source, JobOpening jobOpening) {
        return new JobOpeningEvent(EventType.JOB_OPENING_UPDATED, source, jobOpening);
    }
    
    /**
     * Crée un événement de suppression d'offre d'emploi
     * @param source Source de l'événement
     * @param jobOpening Offre d'emploi supprimée
     * @return Événement de suppression d'offre d'emploi
     */
    public static JobOpeningEvent deleted(String source, JobOpening jobOpening) {
        return new JobOpeningEvent(EventType.JOB_OPENING_DELETED, source, jobOpening);
    }
}
