package com.candiflow.api.unit.observer;

import com.candiflow.api.observer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour l'EventManager
 */
@ExtendWith(MockitoExtension.class)
class EventManagerTest {

    private EventManager eventManager;

    @Mock
    private EventObserver mockObserver1;

    @Mock
    private EventObserver mockObserver2;



    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
        
        // Enregistrement des observateurs
        eventManager.registerObserver(mockObserver1);
        eventManager.registerObserver(mockObserver2);
    }
    
    @Test
    @DisplayName("Devrait notifier les observateurs intéressés par un événement de création d'offre d'emploi")
    void fireEvent_JobOpeningCreated_ShouldNotifyInterestedObservers() {
        // Arrange
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(EventType.JOB_OPENING_CREATED);
        
        when(mockObserver1.isInterestedIn(EventType.JOB_OPENING_CREATED)).thenReturn(true);
        when(mockObserver2.isInterestedIn(EventType.JOB_OPENING_CREATED)).thenReturn(false);
        
        // Act
        eventManager.fireEvent(event);
        
        // Assert
        verify(mockObserver1, times(1)).onEvent(event);
        verify(mockObserver2, never()).onEvent(event);
    }
    
    @Test
    @DisplayName("Devrait notifier les observateurs intéressés par un événement de création de candidat")
    void fireEvent_ApplicantCreated_ShouldNotifyInterestedObservers() {
        // Arrange
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(EventType.APPLICANT_CREATED);
        
        when(mockObserver1.isInterestedIn(EventType.APPLICANT_CREATED)).thenReturn(true);
        when(mockObserver2.isInterestedIn(EventType.APPLICANT_CREATED)).thenReturn(true);
        
        // Act
        eventManager.fireEvent(event);
        
        // Assert
        verify(mockObserver1, times(1)).onEvent(event);
        verify(mockObserver2, times(1)).onEvent(event);
    }
    
    @Test
    @DisplayName("Devrait notifier les observateurs intéressés par un événement de changement d'étape")
    void fireEvent_StageChanged_ShouldNotifyInterestedObservers() {
        // Arrange
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(EventType.STAGE_CHANGED);
        
        when(mockObserver1.isInterestedIn(EventType.STAGE_CHANGED)).thenReturn(false);
        when(mockObserver2.isInterestedIn(EventType.STAGE_CHANGED)).thenReturn(true);
        
        // Act
        eventManager.fireEvent(event);
        
        // Assert
        verify(mockObserver1, never()).onEvent(event);
        verify(mockObserver2, times(1)).onEvent(event);
    }
    
    @Test
    @DisplayName("Devrait désenregistrer un observateur")
    void unregisterObserver_ShouldRemoveObserver() {
        // Arrange
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(EventType.JOB_OPENING_CREATED);
        when(mockObserver1.isInterestedIn(EventType.JOB_OPENING_CREATED)).thenReturn(true);
        
        // Vérifier que l'observateur est bien notifié avant de le désenregistrer
        eventManager.fireEvent(event);
        verify(mockObserver1, times(1)).onEvent(event);
        
        // Act - Désenregistrer l'observateur
        eventManager.unregisterObserver(mockObserver1);
        
        // Réinitialiser les mocks pour éviter les erreurs de vérification
        reset(mockObserver1);
        
        // Assert - Vérifier que l'observateur n'est plus notifié après désenregistrement
        eventManager.fireEvent(event);
        verify(mockObserver1, never()).onEvent(event);
    }
}
