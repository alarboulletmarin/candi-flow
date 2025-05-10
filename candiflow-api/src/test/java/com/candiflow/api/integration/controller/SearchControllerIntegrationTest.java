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
// Import supprimé car non utilisé

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration pour le SearchController
 */
class SearchControllerIntegrationTest extends BaseIntegrationTest {

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
    private JobOpening jobOpening;
    private OpeningApplicant applicant;
    private PipelineStage stage;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        // Nettoyer la base de données
        openingApplicantRepository.deleteAll();
        jobOpeningRepository.deleteAll();
        pipelineStageRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un recruteur
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("recruiter@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setRole(UserRole.RECRUITER);

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        token = authResponse.getAccessToken();

        recruiter = userRepository.findByEmail("recruiter@example.com").orElseThrow();

        // Créer une étape de pipeline
        stage = new PipelineStage();
        stage.setName("APPLIED");
        stage.setDisplayOrder(1);
        stage = pipelineStageRepository.save(stage);

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
        applicant.setApplicationDate(LocalDate.now().minusDays(1));
        applicant.setCreatedAt(Instant.now().minusSeconds(86400));
        applicant.setUpdatedAt(Instant.now());
        applicant = openingApplicantRepository.save(applicant);
    }

    @Test
    @DisplayName("Devrait rechercher des offres d'emploi avec succès")
    void searchJobOpenings_ShouldReturnJobOpenings() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/search/job-openings")
                .header("Authorization", "Bearer " + token)
                .param("title", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", containsString("Java")))
                .andExpect(jsonPath("$.content[0].status", equalTo(JobStatus.OPEN.toString())));
    }

    @Test
    @DisplayName("Devrait rechercher des candidats avec succès")
    void searchApplicants_ShouldReturnApplicants() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/search/applicants")
                .header("Authorization", "Bearer " + token)
                .param("name", "Jean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("Jean")))
                .andExpect(jsonPath("$.content[0].email", equalTo("jean.dupont@example.com")));
    }

    @Test
    @DisplayName("Devrait filtrer les offres d'emploi par statut")
    void searchJobOpenings_ByStatus_ShouldReturnFilteredJobOpenings() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/search/job-openings")
                .header("Authorization", "Bearer " + token)
                .param("status", JobStatus.OPEN.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", equalTo(JobStatus.OPEN.toString())));
    }

    @Test
    @DisplayName("Devrait filtrer les candidats par date de candidature")
    void searchApplicants_ByApplicationDate_ShouldReturnFilteredApplicants() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/search/applicants")
                .header("Authorization", "Bearer " + token)
                .param("appliedAfter", LocalDate.now().minusDays(2).toString())
                .param("appliedBefore", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("Jean")));
    }

    @Test
    @DisplayName("Devrait retourner une liste vide si aucun résultat ne correspond")
    void searchJobOpenings_WithNoMatches_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/search/job-openings")
                .header("Authorization", "Bearer " + token)
                .param("title", "Python"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Devrait retourner 401 si non authentifié")
    void searchJobOpenings_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/search/job-openings")
                .param("title", "Java"))
                .andExpect(status().isUnauthorized());
    }
}
