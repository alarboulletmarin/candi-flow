package com.candiflow.api.integration.controller;

import com.candiflow.api.dto.auth.AuthResponse;
import com.candiflow.api.dto.auth.RegisterRequest;
import com.candiflow.api.integration.BaseIntegrationTest;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.PipelineStage;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
import com.candiflow.api.model.enums.UserRole;
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
// Imports supprimés car non utilisés

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration pour le DashboardController
 */
class DashboardControllerIntegrationTest extends BaseIntegrationTest {

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

    private User recruiter;
    private User admin;
    private JobOpening jobOpening1;
    private JobOpening jobOpening2;
    private OpeningApplicant applicant1;
    private OpeningApplicant applicant2;
    private PipelineStage stage1;
    private PipelineStage stage2;
    private PipelineStage stage3;
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

        // Créer des étapes de pipeline
        stage1 = new PipelineStage();
        stage1.setName("APPLIED");
        stage1.setDisplayOrder(1);
        stage1 = pipelineStageRepository.save(stage1);

        stage2 = new PipelineStage();
        stage2.setName("INTERVIEW");
        stage2.setDisplayOrder(2);
        stage2 = pipelineStageRepository.save(stage2);

        stage3 = new PipelineStage();
        stage3.setName("HIRED");
        stage3.setDisplayOrder(3);
        stage3 = pipelineStageRepository.save(stage3);

        // Créer des offres d'emploi
        jobOpening1 = new JobOpening();
        jobOpening1.setTitle("Développeur Java");
        jobOpening1.setDescription("Description du poste de développeur Java");
        jobOpening1.setStatus(JobStatus.OPEN);
        jobOpening1.setRecruiter(recruiter);
        jobOpening1.setCreatedAt(Instant.now().minusSeconds(86400)); // Hier
        jobOpening1.setUpdatedAt(Instant.now());
        jobOpening1 = jobOpeningRepository.save(jobOpening1);

        jobOpening2 = new JobOpening();
        jobOpening2.setTitle("Développeur Python");
        jobOpening2.setDescription("Description du poste de développeur Python");
        jobOpening2.setStatus(JobStatus.CLOSED);
        jobOpening2.setRecruiter(recruiter);
        jobOpening2.setCreatedAt(Instant.now().minusSeconds(172800)); // Avant-hier
        jobOpening2.setUpdatedAt(Instant.now());
        jobOpening2 = jobOpeningRepository.save(jobOpening2);

        // Créer des candidats
        applicant1 = new OpeningApplicant();
        applicant1.setName("Jean Dupont");
        applicant1.setEmail("jean.dupont@example.com");
        applicant1.setPhone("0123456789");
        applicant1.setJobOpening(jobOpening1);
        applicant1.setCurrentStage(stage1);
        applicant1.setApplicationDate(LocalDate.now().minusDays(1));
        applicant1.setCreatedAt(Instant.now().minusSeconds(86400));
        applicant1.setUpdatedAt(Instant.now());
        applicant1 = openingApplicantRepository.save(applicant1);

        applicant2 = new OpeningApplicant();
        applicant2.setName("Marie Martin");
        applicant2.setEmail("marie.martin@example.com");
        applicant2.setPhone("0987654321");
        applicant2.setJobOpening(jobOpening2);
        applicant2.setCurrentStage(stage3);
        applicant2.setApplicationDate(LocalDate.now().minusDays(30));
        applicant2.setCreatedAt(Instant.now().minusSeconds(2592000)); // 30 jours
        applicant2.setUpdatedAt(Instant.now());
        applicant2 = openingApplicantRepository.save(applicant2);
    }

    @Test
    @DisplayName("Devrait récupérer le tableau de bord du recruteur")
    void getRecruiterDashboard_ShouldReturnDashboardData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/dashboard/recruiter")
                .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobOpenings", equalTo(2)))
                .andExpect(jsonPath("$.activeJobOpenings", equalTo(1)))
                .andExpect(jsonPath("$.totalApplicants", equalTo(2)))
                .andExpect(jsonPath("$.conversionRates", hasSize(2))); // 2 transitions entre 3 étapes
    }

    @Test
    @DisplayName("Devrait récupérer les statistiques d'une offre d'emploi")
    void getJobOpeningStats_ShouldReturnJobOpeningStats() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/dashboard/job-openings/" + jobOpening1.getId())
                .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Développeur Java")))
                .andExpect(jsonPath("$.status", equalTo(JobStatus.OPEN.toString())))
                .andExpect(jsonPath("$.totalApplicants", equalTo(1)))
                .andExpect(jsonPath("$.applicantsByStage", hasSize(1)))
                .andExpect(jsonPath("$.applicantsByStage[0].stageName", equalTo("APPLIED")))
                .andExpect(jsonPath("$.applicantsByStage[0].count", equalTo(1)));
    }

    @Test
    @DisplayName("Devrait récupérer les statistiques globales (admin uniquement)")
    void getGlobalStats_WithAdminRole_ShouldReturnGlobalStats() throws Exception {
        // Vérifier que l'admin est bien créé
        assertThat(admin).isNotNull();
        assertThat(admin.getEmail()).isEqualTo("admin@example.com");
        
        // Act & Assert
        mockMvc.perform(get("/api/dashboard/global")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobOpenings", equalTo(2)))
                .andExpect(jsonPath("$.totalApplicants", equalTo(2)))
                .andExpect(jsonPath("$.jobOpeningsByStatus", hasSize(2))) // OPEN et CLOSED
                .andExpect(jsonPath("$.applicantsByStage", hasSize(2))); // APPLIED et HIRED
    }

    @Test
    @DisplayName("Ne devrait pas permettre aux recruteurs d'accéder aux statistiques globales")
    void getGlobalStats_WithRecruiterRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/dashboard/global")
                .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait retourner 401 si non authentifié")
    void getRecruiterDashboard_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/dashboard/recruiter"))
                .andExpect(status().isUnauthorized());
    }
}
