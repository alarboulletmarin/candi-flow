package com.candiflow.api.observer;

/**
 * Énumération des types d'événements du système
 */
public enum EventType {
    // Événements liés aux offres d'emploi
    JOB_OPENING_CREATED,
    JOB_OPENING_UPDATED,
    JOB_OPENING_DELETED,
    
    // Événements liés aux candidats
    APPLICANT_CREATED,
    APPLICANT_UPDATED,
    APPLICANT_DELETED,
    
    // Événements liés aux étapes du pipeline
    STAGE_CHANGED,
    
    // Événements liés aux utilisateurs
    USER_REGISTERED,
    USER_LOGGED_IN,
    USER_LOGGED_OUT,
    
    // Événements système
    SYSTEM_ERROR,
    SYSTEM_WARNING
}
