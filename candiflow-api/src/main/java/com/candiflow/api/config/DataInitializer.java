package com.candiflow.api.config;

import com.candiflow.api.model.entity.ApplicationStatus;
import com.candiflow.api.repository.ApplicationStatusRepository;
import com.candiflow.api.service.CandidateSourceService;
import com.candiflow.api.service.PipelineStageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Initialise les données par défaut au démarrage de l'application
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ApplicationStatusRepository applicationStatusRepository;
    private final PipelineStageService pipelineStageService;
    private final CandidateSourceService candidateSourceService;

    @Override
    public void run(String... args) {
        log.info("Initialisation des données par défaut...");
        
        initializeApplicationStatuses();
        initializePipelineStages();
        initializeCandidateSources();
        
        log.info("Initialisation des données terminée");
    }

    private void initializeApplicationStatuses() {
        if (applicationStatusRepository.count() == 0) {
            log.info("Initialisation des statuts de candidature par défaut");
            
            List<ApplicationStatus> defaultStatuses = new ArrayList<>();
            
            ApplicationStatus applied = new ApplicationStatus();
            applied.setName("Applied");
            applied.setDescription("Initial application submitted");
            applied.setDisplayOrder(1);
            applied.setActive(true);
            defaultStatuses.add(applied);
            
            ApplicationStatus screened = new ApplicationStatus();
            screened.setName("Resume Screened");
            screened.setDescription("Resume has been reviewed");
            screened.setDisplayOrder(2);
            screened.setActive(true);
            defaultStatuses.add(screened);
            
            ApplicationStatus phoneInterview = new ApplicationStatus();
            phoneInterview.setName("Phone Interview");
            phoneInterview.setDescription("Phone interview scheduled or completed");
            phoneInterview.setDisplayOrder(3);
            phoneInterview.setActive(true);
            defaultStatuses.add(phoneInterview);
            
            ApplicationStatus technicalInterview = new ApplicationStatus();
            technicalInterview.setName("Technical Interview");
            technicalInterview.setDescription("Technical interview scheduled or completed");
            technicalInterview.setDisplayOrder(4);
            technicalInterview.setActive(true);
            defaultStatuses.add(technicalInterview);
            
            ApplicationStatus onsiteInterview = new ApplicationStatus();
            onsiteInterview.setName("Onsite Interview");
            onsiteInterview.setDescription("Onsite interview scheduled or completed");
            onsiteInterview.setDisplayOrder(5);
            onsiteInterview.setActive(true);
            defaultStatuses.add(onsiteInterview);
            
            ApplicationStatus offerExtended = new ApplicationStatus();
            offerExtended.setName("Offer Extended");
            offerExtended.setDescription("Job offer has been extended");
            offerExtended.setDisplayOrder(6);
            offerExtended.setActive(true);
            defaultStatuses.add(offerExtended);
            
            ApplicationStatus offerAccepted = new ApplicationStatus();
            offerAccepted.setName("Offer Accepted");
            offerAccepted.setDescription("Job offer has been accepted");
            offerAccepted.setDisplayOrder(7);
            offerAccepted.setActive(true);
            defaultStatuses.add(offerAccepted);
            
            ApplicationStatus rejected = new ApplicationStatus();
            rejected.setName("Rejected");
            rejected.setDescription("Application has been rejected");
            rejected.setDisplayOrder(8);
            rejected.setActive(true);
            defaultStatuses.add(rejected);
            
            applicationStatusRepository.saveAll(defaultStatuses);
            log.info("Statuts de candidature par défaut initialisés");
        }
    }
    
    private void initializePipelineStages() {
        log.info("Initialisation des étapes du pipeline par défaut");
        pipelineStageService.initializeDefaultStages();
        log.info("Étapes du pipeline par défaut initialisées");
    }
    
    private void initializeCandidateSources() {
        log.info("Initialisation des sources de candidats par défaut");
        candidateSourceService.initializeDefaultSources();
        log.info("Sources de candidats par défaut initialisées");
    }
}
