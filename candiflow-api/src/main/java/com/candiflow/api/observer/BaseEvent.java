package com.candiflow.api.observer;

import lombok.Getter;

import java.time.Instant;

/**
 * Implémentation de base pour les événements
 * Implémente le pattern Observer
 */
@Getter
public abstract class BaseEvent implements Event {
    
    private final EventType type;
    private final Instant timestamp;
    private final String source;
    
    /**
     * Constructeur
     * @param type Type d'événement
     * @param source Source de l'événement
     */
    protected BaseEvent(EventType type, String source) {
        this.type = type;
        this.timestamp = Instant.now();
        this.source = source;
    }
}
