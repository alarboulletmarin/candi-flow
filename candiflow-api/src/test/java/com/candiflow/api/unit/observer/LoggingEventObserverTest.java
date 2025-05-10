package com.candiflow.api.unit.observer;

import com.candiflow.api.observer.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le LoggingEventObserver
 */
@ExtendWith(MockitoExtension.class)
class LoggingEventObserverTest {

    @Mock
    private EventManager eventManager;
    
    @InjectMocks
    private LoggingEventObserver loggingEventObserver;



    @Test
    @DisplayName("Devrait être intéressé par tous les types d'événements")
    void isInterestedIn_ShouldReturnTrueForAllEventTypes() {
        // Act & Assert
        assertTrue(loggingEventObserver.isInterestedIn(EventType.JOB_OPENING_CREATED));
        assertTrue(loggingEventObserver.isInterestedIn(EventType.APPLICANT_CREATED));
        assertTrue(loggingEventObserver.isInterestedIn(EventType.STAGE_CHANGED));
        assertTrue(loggingEventObserver.isInterestedIn(EventType.SYSTEM_ERROR));
        assertTrue(loggingEventObserver.isInterestedIn(EventType.SYSTEM_WARNING));
    }
    
    @Test
    @DisplayName("Devrait s'enregistrer auprès du gestionnaire d'événements lors de l'initialisation")
    void init_ShouldRegisterWithEventManager() {
        // Act
        loggingEventObserver.init();
        
        // Assert
        verify(eventManager).registerObserver(loggingEventObserver);
    }
    

}
