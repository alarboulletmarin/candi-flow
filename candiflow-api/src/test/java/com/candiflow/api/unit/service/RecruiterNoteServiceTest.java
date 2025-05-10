package com.candiflow.api.unit.service;

import com.candiflow.api.dto.recruiter.RecruiterNoteRequest;
import com.candiflow.api.dto.recruiter.RecruiterNoteResponse;
import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.RecruiterNote;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.repository.RecruiterNoteRepository;
import com.candiflow.api.service.RecruiterNoteService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruiterNoteServiceTest {

    @Mock
    private RecruiterNoteRepository recruiterNoteRepository;

    @Mock
    private JobOpeningRepository jobOpeningRepository;

    @Mock
    private OpeningApplicantRepository openingApplicantRepository;

    @InjectMocks
    private RecruiterNoteService recruiterNoteService;

    private UUID jobOpeningId;
    private UUID applicantId;
    private UUID noteId;
    private UUID recruiterId;
    private JobOpening jobOpening;
    private OpeningApplicant applicant;
    private User recruiter;
    private RecruiterNote note;
    private RecruiterNoteRequest noteRequest;

    @BeforeEach
    void setUp() {
        // Initialiser les IDs
        jobOpeningId = UUID.randomUUID();
        applicantId = UUID.randomUUID();
        noteId = UUID.randomUUID();
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

        // Initialiser le candidat
        applicant = new OpeningApplicant();
        applicant.setId(applicantId);
        applicant.setJobOpening(jobOpening);

        // Initialiser la note
        note = new RecruiterNote();
        note.setId(noteId);
        note.setApplicant(applicant);
        note.setAuthor(recruiter);
        note.setNoteText("Candidat très prometteur");
        note.setCreatedAt(Instant.now());

        // Initialiser la requête de note
        noteRequest = new RecruiterNoteRequest();
        noteRequest.setNoteText("Candidat très prometteur");
    }

    @Test
    void getAllNotesByApplicant_ShouldReturnPageOfNotes() {
        // Arrange
        Page<RecruiterNote> notePage = new PageImpl<>(Collections.singletonList(note));
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByApplicant(eq(applicant), any(Pageable.class))).thenReturn(notePage);

        // Act
        Page<RecruiterNoteResponse> result = recruiterNoteService.getAllNotesByApplicant(jobOpeningId, applicantId, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(note.getNoteText(), result.getContent().get(0).getNoteText());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByApplicant(eq(applicant), any(Pageable.class));
    }

    @Test
    void getAllNotesByApplicant_WhenJobOpeningNotFound_ShouldThrowException() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            recruiterNoteService.getAllNotesByApplicant(jobOpeningId, applicantId, Pageable.unpaged())
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verifyNoInteractions(openingApplicantRepository);
        verifyNoInteractions(recruiterNoteRepository);
    }

    @Test
    void getAllNotesByApplicant_WhenApplicantNotFound_ShouldThrowException() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            recruiterNoteService.getAllNotesByApplicant(jobOpeningId, applicantId, Pageable.unpaged())
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verifyNoInteractions(recruiterNoteRepository);
    }

    @Test
    void getAllNotesByApplicantSortedByDate_ShouldReturnListOfNotes() {
        // Arrange
        List<RecruiterNote> notes = Collections.singletonList(note);
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByApplicantOrderByCreatedAtDesc(applicant)).thenReturn(notes);

        // Act
        List<RecruiterNoteResponse> result = recruiterNoteService.getAllNotesByApplicantSortedByDate(jobOpeningId, applicantId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(note.getNoteText(), result.get(0).getNoteText());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByApplicantOrderByCreatedAtDesc(applicant);
    }

    @Test
    void getNoteById_ShouldReturnNote() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)).thenReturn(Optional.of(note));

        // Act
        RecruiterNoteResponse result = recruiterNoteService.getNoteById(jobOpeningId, applicantId, noteId);

        // Assert
        assertNotNull(result);
        assertEquals(note.getNoteText(), result.getNoteText());
        assertEquals(note.getId(), result.getId());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByIdAndApplicant(noteId, applicant);
    }

    @Test
    void getNoteById_WhenNoteNotFound_ShouldThrowException() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            recruiterNoteService.getNoteById(jobOpeningId, applicantId, noteId)
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByIdAndApplicant(noteId, applicant);
    }

    @Test
    void createNote_ShouldCreateAndReturnNote() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.save(any(RecruiterNote.class))).thenReturn(note);

        // Act
        RecruiterNoteResponse result = recruiterNoteService.createNote(jobOpeningId, applicantId, noteRequest, recruiter);

        // Assert
        assertNotNull(result);
        assertEquals(note.getNoteText(), result.getNoteText());
        assertEquals(note.getId(), result.getId());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).save(any(RecruiterNote.class));
    }

    @Test
    void updateNote_ShouldUpdateAndReturnNote() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)).thenReturn(Optional.of(note));
        when(recruiterNoteRepository.save(note)).thenReturn(note);

        // Modifier la requête de note
        noteRequest.setNoteText("Candidat excellent");

        // Act
        RecruiterNoteResponse result = recruiterNoteService.updateNote(jobOpeningId, applicantId, noteId, noteRequest, recruiter);

        // Assert
        assertNotNull(result);
        assertEquals("Candidat excellent", note.getNoteText());
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByIdAndApplicant(noteId, applicant);
        verify(recruiterNoteRepository).save(note);
    }

    @Test
    void updateNote_WhenUserIsNotAuthor_ShouldThrowException() {
        // Arrange
        User otherRecruiter = new User();
        otherRecruiter.setId(UUID.randomUUID());
        otherRecruiter.setRole(UserRole.RECRUITER);

        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)).thenReturn(Optional.of(note));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
            recruiterNoteService.updateNote(jobOpeningId, applicantId, noteId, noteRequest, otherRecruiter)
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByIdAndApplicant(noteId, applicant);
        verify(recruiterNoteRepository, never()).save(any(RecruiterNote.class));
    }

    @Test
    void deleteNote_ShouldDeleteNote() {
        // Arrange
        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)).thenReturn(Optional.of(note));

        // Act
        recruiterNoteService.deleteNote(jobOpeningId, applicantId, noteId, recruiter);

        // Assert
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByIdAndApplicant(noteId, applicant);
        verify(recruiterNoteRepository).delete(note);
    }

    @Test
    void deleteNote_WhenUserIsNotAuthor_ShouldThrowException() {
        // Arrange
        User otherRecruiter = new User();
        otherRecruiter.setId(UUID.randomUUID());
        otherRecruiter.setRole(UserRole.RECRUITER);

        when(jobOpeningRepository.findById(jobOpeningId)).thenReturn(Optional.of(jobOpening));
        when(openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)).thenReturn(Optional.of(applicant));
        when(recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)).thenReturn(Optional.of(note));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
            recruiterNoteService.deleteNote(jobOpeningId, applicantId, noteId, otherRecruiter)
        );
        verify(jobOpeningRepository).findById(jobOpeningId);
        verify(openingApplicantRepository).findByIdAndJobOpening(applicantId, jobOpening);
        verify(recruiterNoteRepository).findByIdAndApplicant(noteId, applicant);
        verify(recruiterNoteRepository, never()).delete(any(RecruiterNote.class));
    }
}
