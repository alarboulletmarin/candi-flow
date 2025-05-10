package com.candiflow.api.unit.controller;

import com.candiflow.api.controller.OpeningApplicantController;
import com.candiflow.api.dto.recruiter.OpeningApplicantRequest;
import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.OpeningApplicantService;
import com.candiflow.api.service.UserService;
import com.candiflow.api.unit.BaseUnitTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpeningApplicantControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;
    
    @Mock
    private OpeningApplicantService openingApplicantService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private OpeningApplicantController openingApplicantController;

    private UUID jobOpeningId;
    private UUID applicantId;
    private UUID stageId;
    private User testRecruiter;
    private OpeningApplicantRequest applicantRequest;
    private OpeningApplicantResponse applicantResponse;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper pour gérer les dates Java 8
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        
        // Configurer MockMvc avec les convertisseurs nécessaires
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(openingApplicantController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .build();
        jobOpeningId = UUID.randomUUID();
        applicantId = UUID.randomUUID();
        stageId = UUID.randomUUID();

        testRecruiter = new User();
        testRecruiter.setId(UUID.randomUUID());
        testRecruiter.setEmail("recruiter@example.com");
        testRecruiter.setName("John Doe");
        testRecruiter.setRole(UserRole.RECRUITER);

        applicantRequest = new OpeningApplicantRequest();
        applicantRequest.setName("Jane Smith");
        applicantRequest.setEmail("jane.smith@example.com");
        applicantRequest.setPhone("+33123456789");
        applicantRequest.setPipelineStageId(stageId);
        applicantRequest.setApplicationDate(LocalDate.now());
        applicantRequest.setInitialNotes("Candidature spontanée");

        OpeningApplicantResponse.PipelineStageSummary stageSummary = OpeningApplicantResponse.PipelineStageSummary.builder()
                .id(stageId)
                .name("Nouveau")
                .isEndStage(false)
                .build();
                
        applicantResponse = OpeningApplicantResponse.builder()
                .id(applicantId)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phone("+33123456789")
                .pipelineStage(stageSummary)
                .applicationDate(LocalDate.now())
                .totalNotes(1)
                .build();
    }

    @Test
    void getAllApplicantsByJobOpening_ShouldReturnApplicants() throws Exception {
        // Arrange
        Page<OpeningApplicantResponse> applicantsPage = new PageImpl<>(Collections.singletonList(applicantResponse));
        when(openingApplicantService.getAllApplicantsByJobOpening(eq(jobOpeningId), any(Pageable.class))).thenReturn(applicantsPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants", jobOpeningId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(applicantId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Jane Smith"));

        verify(openingApplicantService).getAllApplicantsByJobOpening(eq(jobOpeningId), any(Pageable.class));
    }

    @Test
    void searchApplicantsByKeyword_ShouldReturnMatchingApplicants() throws Exception {
        // Arrange
        String keyword = "Java";
        Page<OpeningApplicantResponse> applicantsPage = new PageImpl<>(Collections.singletonList(applicantResponse));
        when(openingApplicantService.searchApplicantsByKeyword(eq(jobOpeningId), eq(keyword), any(Pageable.class))).thenReturn(applicantsPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants/search", jobOpeningId)
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(applicantId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Jane Smith"));

        verify(openingApplicantService).searchApplicantsByKeyword(eq(jobOpeningId), eq(keyword), any(Pageable.class));
    }

    @Test
    void getApplicantById_ShouldReturnApplicant() throws Exception {
        // Arrange
        when(openingApplicantService.getApplicantById(jobOpeningId, applicantId)).thenReturn(applicantResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants/{applicantId}", jobOpeningId, applicantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicantId.toString()))
                .andExpect(jsonPath("$.name").value("Jane Smith"));

        verify(openingApplicantService).getApplicantById(jobOpeningId, applicantId);
    }

    @Test
    void createApplicant_ShouldCreateAndReturnApplicant() throws Exception {
        // Arrange
        when(userService.getUserByEmail("recruiter@example.com")).thenReturn(Optional.of(testRecruiter));
        when(openingApplicantService.createApplicant(eq(jobOpeningId), any(OpeningApplicantRequest.class), eq(testRecruiter))).thenReturn(applicantResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/job-openings/{jobOpeningId}/applicants", jobOpeningId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicantRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(applicantId.toString()))
                .andExpect(jsonPath("$.name").value("Jane Smith"));

        verify(userService).getUserByEmail("recruiter@example.com");
        verify(openingApplicantService).createApplicant(eq(jobOpeningId), any(OpeningApplicantRequest.class), eq(testRecruiter));
    }

    @Test
    void updateApplicant_ShouldUpdateAndReturnApplicant() throws Exception {
        // Arrange
        when(openingApplicantService.updateApplicant(eq(jobOpeningId), eq(applicantId), any(OpeningApplicantRequest.class))).thenReturn(applicantResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/job-openings/{jobOpeningId}/applicants/{applicantId}", jobOpeningId, applicantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicantRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicantId.toString()))
                .andExpect(jsonPath("$.name").value("Jane Smith"));

        verify(openingApplicantService).updateApplicant(eq(jobOpeningId), eq(applicantId), any(OpeningApplicantRequest.class));
    }

    @Test
    void updateApplicantStage_ShouldUpdateAndReturnApplicant() throws Exception {
        // Arrange
        UUID newStageId = UUID.randomUUID();
        String note = "Candidat prêt pour l'entretien";
        when(userService.getUserByEmail("recruiter@example.com")).thenReturn(Optional.of(testRecruiter));
        when(openingApplicantService.updateApplicantStage(eq(jobOpeningId), eq(applicantId), eq(newStageId), eq(testRecruiter), eq(note)))
                .thenReturn(applicantResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/stage/{stageId}", 
                jobOpeningId, applicantId, newStageId)
                .param("note", note))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicantId.toString()))
                .andExpect(jsonPath("$.name").value("Jane Smith"));

        verify(userService).getUserByEmail("recruiter@example.com");
        verify(openingApplicantService).updateApplicantStage(eq(jobOpeningId), eq(applicantId), eq(newStageId), eq(testRecruiter), eq(note));
    }

    @Test
    void deleteApplicant_ShouldDeleteApplicant() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/job-openings/{jobOpeningId}/applicants/{applicantId}", jobOpeningId, applicantId))
                .andExpect(status().isNoContent());

        verify(openingApplicantService).deleteApplicant(jobOpeningId, applicantId);
    }

    @Test
    void getAllApplicantsByJobOpening_WhenNotRecruiter_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants", jobOpeningId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
