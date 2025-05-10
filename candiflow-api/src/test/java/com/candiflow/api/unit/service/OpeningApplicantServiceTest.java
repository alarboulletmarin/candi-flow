package com.candiflow.api.unit.service;

import com.candiflow.api.dto.recruiter.OpeningApplicantRequest;
import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.*;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.*;
import com.candiflow.api.service.OpeningApplicantService;
import com.candiflow.api.service.PipelineStageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpeningApplicantServiceTest {

    @Mock
    private OpeningApplicantRepository openingApplicantRepository;

    @Mock
    private JobOpeningRepository jobOpeningRepository;

    @Mock
    private PipelineStageRepository pipelineStageRepository;

    @Mock
    private CandidateSourceRepository candidateSourceRepository;

    @Mock
    private RecruiterNoteRepository recruiterNoteRepository;

    @Mock
    private PipelineStageService pipelineStageService;

    @InjectMocks
    private OpeningApplicantService openingApplicantService;

    private UUID jobOpeningId;
    private UUID applicantId;
    private UUID stageId;
    private UUID sourceId;
    private UUID recruiterId;
    private JobOpening jobOpening;
    private OpeningApplicant applicant;
    private PipelineStage pipelineStage;
    private PipelineStage newPipelineStage;
    private CandidateSource candidateSource;
    private User recruiter;
    private OpeningApplicantRequest applicantRequest;

    @BeforeEach
    void setUp() {
        // Initialiser les IDs
        jobOpeningId = UUID.randomUUID();
        applicantId = UUID.randomUUID();
        stageId = UUID.randomUUID();
        sourceId = UUID.randomUUID();
        recruiterId = UUID.randomUUID();

        // Initialiser le recruteur
        recruiter = new User();
        recruiter.setId(recruiterId);
        recruiter.setEmail("recruiter@example.com");
        recruiter.setName("John Doe");
        recruiter.setRole(UserRole.RECRUITER);

        // Initialiser l'offre d'emploi
        jobOpening = new JobOpening();
        jobOpening.setId(jobOpeningId);
        jobOpening.setTitle("Développeur Java");
        jobOpening.setRecruiter(recruiter);

        // Initialiser l'étape du pipeline
        pipelineStage = new PipelineStage();
        pipelineStage.setId(stageId);
        pipelineStage.setName("Nouveau");
        pipelineStage.setDisplayOrder(1);

        // Initialiser une nouvelle étape du pipeline
        newPipelineStage = new PipelineStage();
        newPipelineStage.setId(UUID.randomUUID());
        newPipelineStage.setName("Entretien");
        newPipelineStage.setDisplayOrder(2);

        // Initialiser la source du candidat
        candidateSource = new CandidateSource();
        candidateSource.setId(sourceId);
        candidateSource.setName("LinkedIn");

        // Initialiser le candidat
        applicant = new OpeningApplicant();
        applicant.setId(applicantId);
        applicant.setJobOpening(jobOpening);
        applicant.setName("Jane Smith");
        applicant.setEmail("jane.smith@example.com");
        applicant.setPhone("+33123456789");
        applicant.setCurrentStage(pipelineStage);
        applicant.setSource(candidateSource);
        applicant.setApplicationDate(LocalDate.now());
        applicant.setCreatedAt(Instant.now());
        applicant.setUpdatedAt(Instant.now());

        // Initialiser la requête de candidat
        applicantRequest = new OpeningApplicantRequest();
        applicantRequest.setName("Jane Smith");
        applicantRequest.setEmail("jane.smith@example.com");
        applicantRequest.setPhone("+33123456789");
        applicantRequest.setPipelineStageId(stageId);
        applicantRequest.setSourceId(sourceId);
        applicantRequest.setApplicationDate(LocalDate.now());
        applicantRequest.setInitialNotes("Candidature spontanée");
    }

    @Test
    void getAllApplicantsByJobOpening_ShouldReturnPageOfApplicants() {
        // Arrange
        Page<OpeningApplicant> applicantsPage = new PageImpl<>(Collections.singletonList(applicant));
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByJobOpening(eq(jobOpening), any(Pageable.class))).thenReturn(applicantsPage);
        when(recruiterNoteRepository.countByApplicant(applicant)).thenReturn(2L);

        // Act
        Page<OpeningApplicantResponse> result = openingApplicantService.getAllApplicantsByJobOpening(jobOpeningId, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(applicant.getName(), result.getContent().get(0).getName());
        assertEquals(applicant.getEmail(), result.getContent().get(0).getEmail());
        assertEquals(2, result.getContent().get(0).getTotalNotes());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByJobOpening(eq(jobOpening), any(Pageable.class));
        verify(recruiterNoteRepository).countByApplicant(applicant);
    }

    @Test
    void getAllApplicantsByJobOpening_WhenJobOpeningNotFound_ShouldThrowException() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            openingApplicantService.getAllApplicantsByJobOpening(jobOpeningId, Pageable.unpaged())
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verifyNoInteractions(openingApplicantRepository);
    }

    @Test
    void searchApplicantsByKeyword_ShouldReturnMatchingApplicants() {
        // Arrange
        String keyword = "Java";
        Page<OpeningApplicant> applicantsPage = new PageImpl<>(Collections.singletonList(applicant));
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.searchByJobOpeningAndKeyword(eq(jobOpening), eq(keyword), any(Pageable.class)))
                .thenReturn(applicantsPage);
        when(recruiterNoteRepository.countByApplicant(applicant)).thenReturn(2L);

        // Act
        Page<OpeningApplicantResponse> result = openingApplicantService.searchApplicantsByKeyword(jobOpeningId, keyword, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(applicant.getName(), result.getContent().get(0).getName());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).searchByJobOpeningAndKeyword(eq(jobOpening), eq(keyword), any(Pageable.class));
    }

    @Test
    void getApplicantById_ShouldReturnApplicant() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.countByApplicant(applicant)).thenReturn(2L);

        // Act
        OpeningApplicantResponse result = openingApplicantService.getApplicantById(jobOpeningId, applicantId);

        // Assert
        assertNotNull(result);
        assertEquals(applicant.getName(), result.getName());
        assertEquals(applicant.getEmail(), result.getEmail());
        assertEquals(2, result.getTotalNotes());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).countByApplicant(applicant);
    }

    @Test
    void getApplicantById_WhenApplicantNotFound_ShouldThrowException() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            openingApplicantService.getApplicantById(jobOpeningId, applicantId)
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
    }

    @Test
    void createApplicant_ShouldCreateAndReturnApplicant() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.existsByJobOpeningAndEmail(jobOpening, applicantRequest.getEmail())).thenReturn(false);
        when(pipelineStageRepository.findById(stageId)).thenReturn(Optional.of(pipelineStage));
        when(candidateSourceRepository.findById(sourceId)).thenReturn(Optional.of(candidateSource));
        when(openingApplicantRepository.save(any(OpeningApplicant.class))).thenReturn(applicant);
        when(recruiterNoteRepository.countByApplicant(applicant)).thenReturn(1L);

        // Act
        OpeningApplicantResponse result = openingApplicantService.createApplicant(jobOpeningId, applicantRequest, recruiter);

        // Assert
        assertNotNull(result);
        assertEquals(applicant.getName(), result.getName());
        assertEquals(applicant.getEmail(), result.getEmail());
        assertEquals(1, result.getTotalNotes());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).existsByJobOpeningAndEmail(jobOpening, applicantRequest.getEmail());
        verify(pipelineStageRepository).findById(stageId);
        verify(candidateSourceRepository).findById(sourceId);
        verify(openingApplicantRepository).save(any(OpeningApplicant.class));
        verify(recruiterNoteRepository).save(any(RecruiterNote.class));
    }

    @Test
    void createApplicant_WhenEmailAlreadyExists_ShouldThrowException() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.existsByJobOpeningAndEmail(jobOpening, applicantRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            openingApplicantService.createApplicant(jobOpeningId, applicantRequest, recruiter)
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).existsByJobOpeningAndEmail(jobOpening, applicantRequest.getEmail());
        verifyNoMoreInteractions(openingApplicantRepository);
    }

    @Test
    void updateApplicantStage_ShouldUpdateAndReturnApplicant() {
        // Arrange
        String note = "Candidat prêt pour l'entretien";
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(pipelineStageRepository.findById(newPipelineStage.getId())).thenReturn(Optional.of(newPipelineStage));
        when(openingApplicantRepository.save(any(OpeningApplicant.class))).thenReturn(applicant);
        when(recruiterNoteRepository.countByApplicant(applicant)).thenReturn(2L);

        // Act
        OpeningApplicantResponse result = openingApplicantService.updateApplicantStage(
                jobOpeningId, applicantId, newPipelineStage.getId(), recruiter, note);

        // Assert
        assertNotNull(result);
        assertEquals(applicant.getName(), result.getName());
        assertEquals(2, result.getTotalNotes());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(pipelineStageRepository).findById(newPipelineStage.getId());
        verify(recruiterNoteRepository).save(any(RecruiterNote.class));
        verify(openingApplicantRepository).save(any(OpeningApplicant.class));
    }

    @Test
    void deleteApplicant_ShouldDeleteApplicant() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));

        // Act
        openingApplicantService.deleteApplicant(jobOpeningId, applicantId);

        // Assert
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(openingApplicantRepository).delete(applicant);
    }
}
