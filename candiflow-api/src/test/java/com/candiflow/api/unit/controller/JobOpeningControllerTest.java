package com.candiflow.api.unit.controller;

import com.candiflow.api.controller.JobOpeningController;
import com.candiflow.api.dto.recruiter.JobOpeningRequest;
import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.JobOpeningService;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobOpeningControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;
    
    @Mock
    private JobOpeningService jobOpeningService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private JobOpeningController jobOpeningController;

    private UUID jobOpeningId;
    private User testRecruiter;
    private JobOpeningRequest jobOpeningRequest;
    private JobOpeningResponse jobOpeningResponse;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper pour gérer les dates Java 8
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        
        // Configurer MockMvc avec les convertisseurs nécessaires
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(jobOpeningController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .build();
        jobOpeningId = UUID.randomUUID();

        testRecruiter = new User();
        testRecruiter.setId(UUID.randomUUID());
        testRecruiter.setEmail("recruiter@example.com");
        testRecruiter.setName("John Doe");
        testRecruiter.setRole(UserRole.RECRUITER);

        jobOpeningRequest = new JobOpeningRequest();
        jobOpeningRequest.setTitle("Développeur Java Senior");
        jobOpeningRequest.setDescription("Nous recherchons un développeur Java expérimenté");
        jobOpeningRequest.setStatus(JobStatus.OPEN);

        jobOpeningResponse = JobOpeningResponse.builder()
                .id(jobOpeningId)
                .title("Développeur Java Senior")
                .description("Nous recherchons un développeur Java expérimenté")
                .status(JobStatus.OPEN)
                .totalApplicants(0)
                .newApplicants(0)
                .inProcessApplicants(0)
                .endStageApplicants(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getAllJobOpenings_ShouldReturnJobOpenings() throws Exception {
        // Arrange
        Page<JobOpeningResponse> jobOpeningsPage = new PageImpl<>(Collections.singletonList(jobOpeningResponse));
        when(jobOpeningService.getAllJobOpenings(any(Pageable.class))).thenReturn(jobOpeningsPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(jobOpeningId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Développeur Java Senior"));

        verify(jobOpeningService).getAllJobOpenings(any(Pageable.class));
    }

    @Test
    void getAllActiveJobOpenings_ShouldReturnActiveJobOpenings() throws Exception {
        // Arrange
        List<JobOpeningResponse> activeJobOpenings = Collections.singletonList(jobOpeningResponse);
        when(jobOpeningService.getAllActiveJobOpenings()).thenReturn(activeJobOpenings);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(jobOpeningId.toString()))
                .andExpect(jsonPath("$[0].title").value("Développeur Java Senior"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));

        verify(jobOpeningService).getAllActiveJobOpenings();
    }

    @Test
    void getJobOpeningById_ShouldReturnJobOpening() throws Exception {
        // Arrange
        when(jobOpeningService.getJobOpeningById(jobOpeningId)).thenReturn(jobOpeningResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{id}", jobOpeningId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobOpeningId.toString()))
                .andExpect(jsonPath("$.title").value("Développeur Java Senior"));

        verify(jobOpeningService).getJobOpeningById(jobOpeningId);
    }

    @Test
    void createJobOpening_ShouldCreateAndReturnJobOpening() throws Exception {
        // Arrange
        when(userService.getUserByEmail("recruiter@example.com")).thenReturn(Optional.of(testRecruiter));
        when(jobOpeningService.createJobOpening(any(JobOpeningRequest.class), eq(testRecruiter))).thenReturn(jobOpeningResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/job-openings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobOpeningRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(jobOpeningId.toString()))
                .andExpect(jsonPath("$.title").value("Développeur Java Senior"));

        verify(userService).getUserByEmail("recruiter@example.com");
        verify(jobOpeningService).createJobOpening(any(JobOpeningRequest.class), eq(testRecruiter));
    }

    @Test
    void updateJobOpening_ShouldUpdateAndReturnJobOpening() throws Exception {
        // Arrange
        when(jobOpeningService.updateJobOpening(eq(jobOpeningId), any(JobOpeningRequest.class))).thenReturn(jobOpeningResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/job-openings/{id}", jobOpeningId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobOpeningRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobOpeningId.toString()))
                .andExpect(jsonPath("$.title").value("Développeur Java Senior"));

        verify(jobOpeningService).updateJobOpening(eq(jobOpeningId), any(JobOpeningRequest.class));
    }

    @Test
    void deleteJobOpening_ShouldDeleteJobOpening() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/job-openings/{id}", jobOpeningId))
                .andExpect(status().isNoContent());

        verify(jobOpeningService).deleteJobOpening(jobOpeningId);
    }

    @Test
    void getAllJobOpenings_WhenNotRecruiter_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
