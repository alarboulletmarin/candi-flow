package com.candiflow.api.observer;

import com.candiflow.api.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Observateur qui envoie des notifications par email
 * Implémente le pattern Observer
 */
@Component
@Slf4j
public class EmailNotificationObserver implements EventObserver {
    
    private final EventManager eventManager;
    // Aucun service n'est nécessaire pour le moment
    private final Set<EventType> interestedEventTypes;
    
    /**
     * Constructeur
     * @param eventManager Gestionnaire d'événements
     */
    public EmailNotificationObserver(EventManager eventManager) {
        this.eventManager = eventManager;
        this.interestedEventTypes = new HashSet<>(Arrays.asList(
                EventType.JOB_OPENING_CREATED,
                EventType.APPLICANT_CREATED,
                EventType.STAGE_CHANGED
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
                notifyJobOpeningCreated((JobOpeningEvent) event);
                break;
            case APPLICANT_CREATED:
                notifyApplicantCreated((ApplicantEvent) event);
                break;
            case STAGE_CHANGED:
                notifyStageChanged((ApplicantEvent) event);
                break;
            default:
                log.debug("Événement non géré pour les notifications email: {}", event.getType());
        }
    }
    
    @Override
    public boolean isInterestedIn(EventType eventType) {
        return interestedEventTypes.contains(eventType);
    }
    
    private void notifyJobOpeningCreated(JobOpeningEvent event) {
        // Dans une implémentation réelle, on enverrait un email
        // Ici, on simule juste l'envoi
        log.info("Simulation d'envoi d'email: Nouvelle offre d'emploi créée - {} (ID: {})",
                event.getJobOpening().getTitle(),
                event.getJobOpening().getId());
        
        // Dans une implémentation réelle, on enverrait un email à tous les recruteurs
        // Pour l'instant, on simule juste l'envoi
        log.info("Notification aux recruteurs: Nouvelle offre d'emploi créée - {} (ID: {})", 
                event.getJobOpening().getTitle(), 
                event.getJobOpening().getId());
    }
    
    private void notifyApplicantCreated(ApplicantEvent event) {
        // Récupérer le recruteur de l'offre d'emploi
        User recruiter = event.getApplicant().getJobOpening().getRecruiter();
        
        // Simuler l'envoi d'un email au recruteur
        log.info("Simulation d'envoi d'email à {}: Nouveau candidat - {} (ID: {}) pour l'offre {} (ID: {})",
                recruiter.getEmail(),
                event.getApplicant().getName(),
                event.getApplicant().getId(),
                event.getApplicant().getJobOpening().getTitle(),
                event.getApplicant().getJobOpening().getId());
    }
    
    private void notifyStageChanged(ApplicantEvent event) {
        // Récupérer le recruteur de l'offre d'emploi
        User recruiter = event.getApplicant().getJobOpening().getRecruiter();
        
        // Simuler l'envoi d'un email au recruteur
        log.info("Simulation d'envoi d'email à {}: Changement d'étape pour le candidat - {} (ID: {}) - {} -> {}",
                recruiter.getEmail(),
                event.getApplicant().getName(),
                event.getApplicant().getId(),
                event.getPreviousStage() != null ? event.getPreviousStage().getName() : "N/A",
                event.getApplicant().getCurrentStage().getName());
        
        // Si le candidat a été embauché, on pourrait envoyer un email de félicitations
        if (event.getApplicant().getCurrentStage().getName().equals("HIRED")) {
            log.info("Simulation d'envoi d'email à {}: Félicitations pour votre embauche chez nous!",
                    event.getApplicant().getEmail());
        }
    }
    
    /**
     * Méthode pour envoyer un email (simulée)
     * @param to Destinataire
     * @param subject Sujet
     * @param body Corps du message
     */
    /**
     * Méthode pour envoyer un email (simulée)
     * Cette méthode est prévue pour une implémentation future avec JavaMailSender
     * @param to Destinataire
     * @param subject Sujet
     * @param body Corps du message
     */
    protected void sendEmail(String to, String subject, String body) {
        // Dans une implémentation réelle, on utiliserait JavaMailSender
        log.info("Simulation d'envoi d'email à {}: Sujet: {}", to, subject);
    }
}
