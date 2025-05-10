package com.candiflow.api.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Observateur qui journalise les événements
 * Implémente le pattern Observer
 */
@Component
@Slf4j
public class LoggingEventObserver implements EventObserver {
    
    private final EventManager eventManager;
    private final Set<EventType> interestedEventTypes;
    
    /**
     * Constructeur
     * @param eventManager Gestionnaire d'événements
     */
    public LoggingEventObserver(EventManager eventManager) {
        this.eventManager = eventManager;
        this.interestedEventTypes = new HashSet<>(Arrays.asList(
                EventType.JOB_OPENING_CREATED,
                EventType.JOB_OPENING_UPDATED,
                EventType.JOB_OPENING_DELETED,
                EventType.APPLICANT_CREATED,
                EventType.APPLICANT_UPDATED,
                EventType.APPLICANT_DELETED,
                EventType.STAGE_CHANGED,
                EventType.SYSTEM_ERROR,
                EventType.SYSTEM_WARNING
        ));
    }
    
    /**
     * Initialisation : s'enregistre auprès du gestionnaire d'événements
     */
    @PostConstruct
    public void init() {
        eventManager.registerObserver(this);
    }
    
    @Override
    public void onEvent(Event event) {
        switch (event.getType()) {
            case JOB_OPENING_CREATED:
                logJobOpeningCreated((JobOpeningEvent) event);
                break;
            case JOB_OPENING_UPDATED:
                logJobOpeningUpdated((JobOpeningEvent) event);
                break;
            case JOB_OPENING_DELETED:
                logJobOpeningDeleted((JobOpeningEvent) event);
                break;
            case APPLICANT_CREATED:
                logApplicantCreated((ApplicantEvent) event);
                break;
            case APPLICANT_UPDATED:
                logApplicantUpdated((ApplicantEvent) event);
                break;
            case APPLICANT_DELETED:
                logApplicantDeleted((ApplicantEvent) event);
                break;
            case STAGE_CHANGED:
                logStageChanged((ApplicantEvent) event);
                break;
            case SYSTEM_ERROR:
                logSystemError(event);
                break;
            case SYSTEM_WARNING:
                logSystemWarning(event);
                break;
            default:
                log.debug("Événement non géré: {}", event.getType());
        }
    }
    
    @Override
    public boolean isInterestedIn(EventType eventType) {
        return interestedEventTypes.contains(eventType);
    }
    
    private void logJobOpeningCreated(JobOpeningEvent event) {
        log.info("Offre d'emploi créée: {} (ID: {})", 
                event.getJobOpening().getTitle(), 
                event.getJobOpening().getId());
    }
    
    private void logJobOpeningUpdated(JobOpeningEvent event) {
        log.info("Offre d'emploi mise à jour: {} (ID: {})", 
                event.getJobOpening().getTitle(), 
                event.getJobOpening().getId());
    }
    
    private void logJobOpeningDeleted(JobOpeningEvent event) {
        log.info("Offre d'emploi supprimée: {} (ID: {})", 
                event.getJobOpening().getTitle(), 
                event.getJobOpening().getId());
    }
    
    private void logApplicantCreated(ApplicantEvent event) {
        log.info("Candidat créé: {} (ID: {}) pour l'offre: {} (ID: {})", 
                event.getApplicant().getName(), 
                event.getApplicant().getId(),
                event.getApplicant().getJobOpening().getTitle(),
                event.getApplicant().getJobOpening().getId());
    }
    
    private void logApplicantUpdated(ApplicantEvent event) {
        log.info("Candidat mis à jour: {} (ID: {})", 
                event.getApplicant().getName(), 
                event.getApplicant().getId());
    }
    
    private void logApplicantDeleted(ApplicantEvent event) {
        log.info("Candidat supprimé: {} (ID: {})", 
                event.getApplicant().getName(), 
                event.getApplicant().getId());
    }
    
    private void logStageChanged(ApplicantEvent event) {
        log.info("Étape du candidat changée: {} (ID: {}) - {} -> {}", 
                event.getApplicant().getName(), 
                event.getApplicant().getId(),
                event.getPreviousStage() != null ? event.getPreviousStage().getName() : "N/A",
                event.getApplicant().getCurrentStage().getName());
    }
    
    private void logSystemError(Event event) {
        log.error("Erreur système: {}", event.getSource());
    }
    
    private void logSystemWarning(Event event) {
        log.warn("Avertissement système: {}", event.getSource());
    }
}
