package com.candiflow.api.unit.service;

import com.candiflow.api.model.entity.CandidateSource;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.PipelineStage;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.service.ExportService;
import com.candiflow.api.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour l'ExportService
 */
class ExportServiceTest extends BaseUnitTest {

    @Mock
    private JobOpeningRepository jobOpeningRepository;

    @Mock
    private OpeningApplicantRepository openingApplicantRepository;

    @InjectMocks
    private ExportService exportService;

    private User recruiter;
    private User anotherRecruiter;
    private JobOpening jobOpening;
    private OpeningApplicant applicant;
    private PipelineStage stage;
    private CandidateSource source;

    @BeforeEach
    void setUp() {
        // Création des utilisateurs
        recruiter = new User();
        recruiter.setId(UUID.randomUUID());
        recruiter.setEmail("recruiter@example.com");
        recruiter.setRole(UserRole.RECRUITER);

        anotherRecruiter = new User();
        anotherRecruiter.setId(UUID.randomUUID());
        anotherRecruiter.setEmail("another.recruiter@example.com");
        anotherRecruiter.setRole(UserRole.RECRUITER);

        // Création d'une étape de pipeline
        stage = new PipelineStage();
        stage.setId(UUID.randomUUID());
        stage.setName("APPLIED");
        stage.setDisplayOrder(1);

        // Création d'une source de candidat
        source = new CandidateSource();
        source.setId(UUID.randomUUID());
        source.setName("LinkedIn");
        source.setDescription("Recrutement via LinkedIn");

        // Création d'une offre d'emploi
        jobOpening = new JobOpening();
        jobOpening.setId(UUID.randomUUID());
        jobOpening.setTitle("Développeur Java");
        jobOpening.setDescription("Description du poste de développeur Java");
        jobOpening.setStatus(JobStatus.OPEN);
        jobOpening.setRecruiter(recruiter);
        jobOpening.setCreatedAt(Instant.now().minusSeconds(86400)); // Hier
        jobOpening.setUpdatedAt(Instant.now());

        // Création d'un candidat
        applicant = new OpeningApplicant();
        applicant.setId(UUID.randomUUID());
        applicant.setName("Jean Dupont");
        applicant.setEmail("jean.dupont@example.com");
        applicant.setPhone("0123456789");
        applicant.setJobOpening(jobOpening);
        applicant.setCurrentStage(stage);
        applicant.setSource(source);
        applicant.setApplicationDate(LocalDate.now().minusDays(1));
        applicant.setCreatedAt(Instant.now().minusSeconds(86400));
        applicant.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Devrait exporter les candidats d'une offre d'emploi au format CSV")
    void exportApplicantsToCSV_ShouldReturnCSVContent() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpening.getId())).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByJobOpening(jobOpening)).thenReturn(Collections.singletonList(applicant));

        // Act
        ByteArrayInputStream result = exportService.exportApplicantsToCSV(jobOpening.getId(), recruiter);

        // Assert
        String csvContent = new BufferedReader(new InputStreamReader(result))
                .lines().collect(Collectors.joining("\n"));
        
        assertThat(csvContent).isNotEmpty();
        assertThat(csvContent).contains("ID,Prénom,Nom,Email,Téléphone,Étape,Source,Date de candidature,Dernière mise à jour");
        assertThat(csvContent).contains(applicant.getId().toString());
        assertThat(csvContent).contains(applicant.getName());
        assertThat(csvContent).contains(applicant.getEmail());
        assertThat(csvContent).contains(applicant.getPhone());
        assertThat(csvContent).contains(stage.getName());
        assertThat(csvContent).contains(source.getName());
    }

    @Test
    @DisplayName("Devrait lancer une exception si le recruteur n'est pas autorisé à exporter les données")
    void exportApplicantsToCSV_WithUnauthorizedRecruiter_ShouldThrowException() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpening.getId())).thenReturn(Optional.of(jobOpening));

        // Act & Assert
        assertThatThrownBy(() -> exportService.exportApplicantsToCSV(jobOpening.getId(), anotherRecruiter))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vous n'êtes pas autorisé à exporter les données de cette offre");
    }

    @Test
    @DisplayName("Devrait exporter toutes les offres d'emploi d'un recruteur au format CSV")
    void exportJobOpeningsToCSV_ShouldReturnCSVContent() {
        // Arrange
        List<JobOpening> jobOpenings = Collections.singletonList(jobOpening);
        when(jobOpeningRepository.findByRecruiterOrderByCreatedAtDesc(recruiter)).thenReturn(jobOpenings);
        when(openingApplicantRepository.countByJobOpening(jobOpening)).thenReturn(1L);

        // Act
        ByteArrayInputStream result = exportService.exportJobOpeningsToCSV(recruiter);

        // Assert
        String csvContent = new BufferedReader(new InputStreamReader(result))
                .lines().collect(Collectors.joining("\n"));
        
        assertThat(csvContent).isNotEmpty();
        assertThat(csvContent).contains("ID,Titre,Description,Statut,Date de création,Nombre de candidats");
        assertThat(csvContent).contains(jobOpening.getId().toString());
        assertThat(csvContent).contains(jobOpening.getTitle());
        assertThat(csvContent).contains(jobOpening.getDescription());
        assertThat(csvContent).contains(jobOpening.getStatus().toString());
        assertThat(csvContent).contains("1"); // Nombre de candidats
    }

    @Test
    @DisplayName("Devrait exporter les statistiques globales au format CSV")
    void exportGlobalStatsToCSV_ShouldReturnCSVContent() {
        // Arrange
        List<JobOpening> allJobOpenings = Collections.singletonList(jobOpening);
        when(jobOpeningRepository.findAll()).thenReturn(allJobOpenings);
        when(openingApplicantRepository.countByJobOpening(jobOpening)).thenReturn(1L);

        // Act
        ByteArrayInputStream result = exportService.exportGlobalStatsToCSV();

        // Assert
        String csvContent = new BufferedReader(new InputStreamReader(result))
                .lines().collect(Collectors.joining("\n"));
        
        assertThat(csvContent).isNotEmpty();
        assertThat(csvContent).contains("ID Offre,Titre,Recruteur,Statut,Date de création,Nombre de candidats");
        assertThat(csvContent).contains(jobOpening.getId().toString());
        assertThat(csvContent).contains(jobOpening.getTitle());
        assertThat(csvContent).contains(recruiter.getEmail());
        assertThat(csvContent).contains(jobOpening.getStatus().toString());
        assertThat(csvContent).contains("1"); // Nombre de candidats
    }
}
