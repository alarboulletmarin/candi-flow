package com.candiflow.api.observer;

/**
 * Interface pour les observateurs d'événements
 * Implémente le pattern Observer
 */
public interface EventObserver {
    
    /**
     * Méthode appelée lorsqu'un événement est déclenché
     * @param event Événement déclenché
     */
    void onEvent(Event event);
    
    /**
     * Indique si l'observateur est intéressé par un type d'événement
     * @param eventType Type d'événement
     * @return true si l'observateur est intéressé, false sinon
     */
    boolean isInterestedIn(EventType eventType);
}
