package com.candiflow.api.unit.observer;

import com.candiflow.api.observer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour l'EmailNotificationObserver
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationObserverTest {

    @Mock
    private EventManager eventManager;
    
    @InjectMocks
    private EmailNotificationObserver emailNotificationObserver;
    
    @Test
    @DisplayName("Devrait être intéressé par les événements de création d'offre d'emploi")
    void isInterestedIn_JobOpeningCreated_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(emailNotificationObserver.isInterestedIn(EventType.JOB_OPENING_CREATED));
    }
    
    @Test
    @DisplayName("Devrait être intéressé par les événements de création de candidat")
    void isInterestedIn_ApplicantCreated_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(emailNotificationObserver.isInterestedIn(EventType.APPLICANT_CREATED));
    }
    
    @Test
    @DisplayName("Devrait être intéressé par les événements de changement d'étape")
    void isInterestedIn_StageChanged_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(emailNotificationObserver.isInterestedIn(EventType.STAGE_CHANGED));
    }
    
    @Test
    @DisplayName("Devrait s'enregistrer auprès du gestionnaire d'événements lors de l'initialisation")
    void init_ShouldRegisterWithEventManager() {
        // Act
        emailNotificationObserver.init();
        
        // Assert
        verify(eventManager).registerObserver(emailNotificationObserver);
    }
    

}
