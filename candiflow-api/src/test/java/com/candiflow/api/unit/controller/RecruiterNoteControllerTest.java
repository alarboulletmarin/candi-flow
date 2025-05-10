package com.candiflow.api.unit.controller;

import com.candiflow.api.controller.RecruiterNoteController;
import com.candiflow.api.dto.recruiter.RecruiterNoteRequest;
import com.candiflow.api.dto.recruiter.RecruiterNoteResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.RecruiterNoteService;
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

class RecruiterNoteControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private RecruiterNoteService recruiterNoteService;

    @Mock
    private UserService userService;
    
    @InjectMocks
    private RecruiterNoteController recruiterNoteController;

    private UUID jobOpeningId;
    private UUID applicantId;
    private UUID noteId;
    private User testRecruiter;
    private RecruiterNoteRequest noteRequest;
    private RecruiterNoteResponse noteResponse;
    private RecruiterNoteResponse.AuthorSummary authorSummary;

    @BeforeEach
    void setUp() {
        // Initialiser les IDs et les objets de test
        jobOpeningId = UUID.randomUUID();
        applicantId = UUID.randomUUID();
        noteId = UUID.randomUUID();

        testRecruiter = new User();
        testRecruiter.setId(UUID.randomUUID());
        testRecruiter.setEmail("recruiter@example.com");
        testRecruiter.setName("John Doe");
        testRecruiter.setRole(UserRole.RECRUITER);

        authorSummary = RecruiterNoteResponse.AuthorSummary.builder()
                .id(testRecruiter.getId())
                .name(testRecruiter.getName())
                .email(testRecruiter.getEmail())
                .build();

        noteRequest = new RecruiterNoteRequest();
        noteRequest.setNoteText("Candidat très prometteur");

        noteResponse = RecruiterNoteResponse.builder()
                .id(noteId)
                .applicantId(applicantId)
                .noteText("Candidat très prometteur")
                .createdAt(Instant.now())
                .author(authorSummary)
                .build();
        
        // Configure ObjectMapper pour gérer les dates Java 8
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        
        // Configurer MockMvc avec les convertisseurs nécessaires
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        // Configurer le mock pour simuler l'authentification
        when(userService.getUserByEmail("recruiter@example.com")).thenReturn(Optional.of(testRecruiter));
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(recruiterNoteController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void getAllNotesByApplicant_ShouldReturnNotes() throws Exception {
        // Arrange
        Page<RecruiterNoteResponse> notePage = new PageImpl<>(Collections.singletonList(noteResponse));
        when(recruiterNoteService.getAllNotesByApplicant(eq(jobOpeningId), eq(applicantId), any(Pageable.class))).thenReturn(notePage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes", jobOpeningId, applicantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(noteId.toString()))
                .andExpect(jsonPath("$.content[0].noteText").value("Candidat très prometteur"));

        verify(recruiterNoteService).getAllNotesByApplicant(eq(jobOpeningId), eq(applicantId), any(Pageable.class));
    }

    @Test
    void getAllNotesByApplicantSortedByDate_ShouldReturnNotes() throws Exception {
        // Arrange
        List<RecruiterNoteResponse> notes = Collections.singletonList(noteResponse);
        when(recruiterNoteService.getAllNotesByApplicantSortedByDate(jobOpeningId, applicantId)).thenReturn(notes);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes/timeline", jobOpeningId, applicantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(noteId.toString()))
                .andExpect(jsonPath("$[0].noteText").value("Candidat très prometteur"));

        verify(recruiterNoteService).getAllNotesByApplicantSortedByDate(jobOpeningId, applicantId);
    }

    @Test
    void getNoteById_ShouldReturnNote() throws Exception {
        // Arrange
        when(recruiterNoteService.getNoteById(jobOpeningId, applicantId, noteId)).thenReturn(noteResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes/{noteId}", jobOpeningId, applicantId, noteId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId.toString()))
                .andExpect(jsonPath("$.noteText").value("Candidat très prometteur"));

        verify(recruiterNoteService).getNoteById(jobOpeningId, applicantId, noteId);
    }

    @Test
    void createNote_ShouldCreateAndReturnNote() throws Exception {
        // Arrange
        when(userService.getUserByEmail("recruiter@example.com")).thenReturn(Optional.of(testRecruiter));
        when(recruiterNoteService.createNote(eq(jobOpeningId), eq(applicantId), any(RecruiterNoteRequest.class), eq(testRecruiter))).thenReturn(noteResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes", jobOpeningId, applicantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(noteId.toString()))
                .andExpect(jsonPath("$.noteText").value("Candidat très prometteur"));

        verify(userService).getUserByEmail("recruiter@example.com");
        verify(recruiterNoteService).createNote(eq(jobOpeningId), eq(applicantId), any(RecruiterNoteRequest.class), eq(testRecruiter));
    }

    @Test
    void updateNote_ShouldUpdateAndReturnNote() throws Exception {
        // Arrange
        when(userService.getUserByEmail("recruiter@example.com")).thenReturn(Optional.of(testRecruiter));
        when(recruiterNoteService.updateNote(eq(jobOpeningId), eq(applicantId), eq(noteId), any(RecruiterNoteRequest.class), eq(testRecruiter))).thenReturn(noteResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes/{noteId}", jobOpeningId, applicantId, noteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId.toString()))
                .andExpect(jsonPath("$.noteText").value("Candidat très prometteur"));

        verify(userService).getUserByEmail("recruiter@example.com");
        verify(recruiterNoteService).updateNote(eq(jobOpeningId), eq(applicantId), eq(noteId), any(RecruiterNoteRequest.class), eq(testRecruiter));
    }

    @Test
    void deleteNote_ShouldDeleteNote() throws Exception {
        // Arrange
        when(userService.getUserByEmail("recruiter@example.com")).thenReturn(Optional.of(testRecruiter));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes/{noteId}", jobOpeningId, applicantId, noteId))
                .andExpect(status().isNoContent());

        verify(userService).getUserByEmail("recruiter@example.com");
        verify(recruiterNoteService).deleteNote(jobOpeningId, applicantId, noteId, testRecruiter);
    }

    @Test
    void getAllNotesByApplicant_WhenNotRecruiter_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes", jobOpeningId, applicantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
