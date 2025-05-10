package com.candiflow.api.integration.controller;

import com.candiflow.api.dto.auth.LoginRequest;
import com.candiflow.api.dto.auth.RegisterRequest;
import com.candiflow.api.integration.BaseIntegrationTest;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration pour le AuthController
 */
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données avant chaque test
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Devrait enregistrer un nouvel utilisateur et retourner un token JWT")
    void register_WithValidRequest_ShouldReturnToken() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setRole(UserRole.CANDIDATE);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        // Vérifier que l'utilisateur a été créé en base de données
        assertThat(userRepository.findByEmail(EMAIL)).isPresent();
    }

    @Test
    @DisplayName("Devrait connecter un utilisateur existant et retourner un token JWT")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange - Créer un utilisateur
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(EMAIL);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setRole(UserRole.CANDIDATE);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Act - Connecter l'utilisateur
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(EMAIL);
        loginRequest.setPassword(PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Devrait retourner 401 pour des identifiants invalides")
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Devrait retourner les informations de l'utilisateur connecté")
    void getCurrentUser_WithValidToken_ShouldReturnUserInfo() throws Exception {
        // Arrange - Créer un utilisateur et récupérer son token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(EMAIL);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setRole(UserRole.CANDIDATE);

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = registerResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value(UserRole.CANDIDATE.toString()));
    }

    @Test
    @DisplayName("Devrait retourner 401 pour un utilisateur non authentifié")
    void getCurrentUser_WithoutToken_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
