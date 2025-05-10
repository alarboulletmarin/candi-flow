package com.candiflow.api.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gestionnaire d'événements
 * Implémente le pattern Observer pour la gestion des événements
 */
@Component
@Slf4j
public class EventManager {
    
    private final List<EventObserver> observers = new CopyOnWriteArrayList<>();
    
    /**
     * Enregistre un observateur
     * @param observer Observateur à enregistrer
     */
    public void registerObserver(EventObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.info("Observateur enregistré: {}", observer.getClass().getSimpleName());
        }
    }
    
    /**
     * Désenregistre un observateur
     * @param observer Observateur à désenregistrer
     */
    public void unregisterObserver(EventObserver observer) {
        observers.remove(observer);
        log.info("Observateur désenregistré: {}", observer.getClass().getSimpleName());
    }
    
    /**
     * Déclenche un événement
     * @param event Événement à déclencher
     */
    public void fireEvent(Event event) {
        log.debug("Événement déclenché: {} depuis {}", event.getType(), event.getSource());
        
        List<EventObserver> interestedObservers = new ArrayList<>();
        
        // Trouver les observateurs intéressés par cet événement
        for (EventObserver observer : observers) {
            if (observer.isInterestedIn(event.getType())) {
                interestedObservers.add(observer);
            }
        }
        
        // Notifier les observateurs intéressés
        for (EventObserver observer : interestedObservers) {
            try {
                observer.onEvent(event);
            } catch (Exception e) {
                log.error("Erreur lors de la notification de l'observateur {}: {}", 
                        observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
