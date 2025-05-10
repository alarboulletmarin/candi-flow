package com.candiflow.api.integration.controller;

import com.candiflow.api.dto.auth.AuthResponse;
import com.candiflow.api.dto.auth.RegisterRequest;
import com.candiflow.api.integration.BaseIntegrationTest;
import com.candiflow.api.model.entity.CandidateSource;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.PipelineStage;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.CandidateSourceRepository;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.repository.PipelineStageRepository;
import com.candiflow.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
// Import supprimé car non utilisé

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le ExportController
 */
class ExportControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobOpeningRepository jobOpeningRepository;

    @Autowired
    private OpeningApplicantRepository openingApplicantRepository;

    @Autowired
    private PipelineStageRepository pipelineStageRepository;
    
    @Autowired
    private CandidateSourceRepository candidateSourceRepository;

    private User recruiter;
    private User admin;
    private JobOpening jobOpening;
    private OpeningApplicant applicant;
    private PipelineStage stage;
    private CandidateSource source;
    private String recruiterToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Nettoyer la base de données
        openingApplicantRepository.deleteAll();
        jobOpeningRepository.deleteAll();
        pipelineStageRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un recruteur
        RegisterRequest recruiterRequest = new RegisterRequest();
        recruiterRequest.setEmail("recruiter@example.com");
        recruiterRequest.setPassword("Password123!");
        recruiterRequest.setRole(UserRole.RECRUITER);

        MvcResult recruiterResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recruiterRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse recruiterResponse = objectMapper.readValue(
                recruiterResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        recruiterToken = recruiterResponse.getAccessToken();
        recruiter = userRepository.findByEmail("recruiter@example.com").orElseThrow();

        // Créer un admin
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setEmail("admin@example.com");
        adminRequest.setPassword("Password123!");
        adminRequest.setRole(UserRole.ADMIN);

        MvcResult adminResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse adminResponse = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        adminToken = adminResponse.getAccessToken();
        admin = userRepository.findByEmail("admin@example.com").orElseThrow();

        // Créer une étape de pipeline
        stage = new PipelineStage();
        stage.setName("APPLIED");
        stage.setDisplayOrder(1);
        stage = pipelineStageRepository.save(stage);

        // Créer une source de candidat
        source = new CandidateSource();
        source.setName("LinkedIn");
        source.setDescription("Recrutement via LinkedIn");
        source = candidateSourceRepository.save(source);

        // Créer une offre d'emploi
        jobOpening = new JobOpening();
        jobOpening.setTitle("Développeur Java");
        jobOpening.setDescription("Description du poste de développeur Java");
        jobOpening.setStatus(JobStatus.OPEN);
        jobOpening.setRecruiter(recruiter);
        jobOpening.setCreatedAt(Instant.now().minusSeconds(86400)); // Hier
        jobOpening.setUpdatedAt(Instant.now());
        jobOpening = jobOpeningRepository.save(jobOpening);

        // Créer un candidat
        applicant = new OpeningApplicant();
        applicant.setName("Jean Dupont");
        applicant.setEmail("jean.dupont@example.com");
        applicant.setPhone("0123456789");
        applicant.setJobOpening(jobOpening);
        applicant.setCurrentStage(stage);
        applicant.setSource(source);
        applicant.setApplicationDate(LocalDate.now().minusDays(1));
        applicant.setCreatedAt(Instant.now().minusSeconds(86400));
        applicant.setUpdatedAt(Instant.now());
        applicant = openingApplicantRepository.save(applicant);
    }

    @Test
    @DisplayName("Devrait exporter les candidats d'une offre d'emploi au format CSV")
    void exportApplicants_ShouldReturnCSVFile() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/export/job-openings/" + jobOpening.getId() + "/applicants")
                .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=applicants_")))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @DisplayName("Devrait exporter toutes les offres d'emploi d'un recruteur au format CSV")
    void exportJobOpenings_ShouldReturnCSVFile() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/export/job-openings")
                .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=job_openings_")))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().contentType("text/csv"));
    }
    


    @Test
    @DisplayName("Devrait exporter les statistiques globales au format CSV (admin uniquement)")
    void exportGlobalStats_WithAdminRole_ShouldReturnCSVFile() throws Exception {
        // Vérifier que l'admin est bien créé
        assertThat(admin).isNotNull();
        assertThat(admin.getEmail()).isEqualTo("admin@example.com");
        
        // Act & Assert
        mockMvc.perform(get("/api/export/global-stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=global_stats_")))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @DisplayName("Ne devrait pas permettre aux recruteurs d'accéder aux statistiques globales")
    void exportGlobalStats_WithRecruiterRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/export/global-stats")
                .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait retourner 401 si non authentifié")
    void exportJobOpenings_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/export/job-openings"))
                .andExpect(status().isUnauthorized());
    }
}
