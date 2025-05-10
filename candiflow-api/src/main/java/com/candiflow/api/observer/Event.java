package com.candiflow.api.observer;

import java.time.Instant;

/**
 * Interface pour les événements du système
 * Implémente le pattern Observer
 */
public interface Event {
    
    /**
     * Récupère le type d'événement
     * @return Type d'événement
     */
    EventType getType();
    
    /**
     * Récupère la date de l'événement
     * @return Date de l'événement
     */
    Instant getTimestamp();
    
    /**
     * Récupère la source de l'événement
     * @return Source de l'événement
     */
    String getSource();
}
