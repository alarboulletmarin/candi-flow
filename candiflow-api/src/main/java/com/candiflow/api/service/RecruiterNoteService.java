package com.candiflow.api.service;

import com.candiflow.api.dto.recruiter.RecruiterNoteRequest;
import com.candiflow.api.dto.recruiter.RecruiterNoteResponse;
import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.RecruiterNote;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.repository.RecruiterNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterNoteService {

    private final RecruiterNoteRepository recruiterNoteRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final OpeningApplicantRepository openingApplicantRepository;

    /**
     * Récupère toutes les notes pour un candidat avec pagination
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @param pageable Informations de pagination
     * @return Page de notes
     */
    @Transactional(readOnly = true)
    public Page<RecruiterNoteResponse> getAllNotesByApplicant(UUID jobOpeningId, UUID applicantId, Pageable pageable) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        return recruiterNoteRepository.findByApplicant(applicant, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Récupère toutes les notes pour un candidat triées par date de création (descendante)
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @return Liste de notes
     */
    @Transactional(readOnly = true)
    public List<RecruiterNoteResponse> getAllNotesByApplicantSortedByDate(UUID jobOpeningId, UUID applicantId) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        return recruiterNoteRepository.findByApplicantOrderByCreatedAtDesc(applicant).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une note par son ID
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @param noteId ID de la note
     * @return La note si elle existe
     */
    @Transactional(readOnly = true)
    public RecruiterNoteResponse getNoteById(UUID jobOpeningId, UUID applicantId, UUID noteId) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        RecruiterNote note = recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvée avec l'ID: " + noteId));
        
        return mapToResponse(note);
    }

    /**
     * Crée une nouvelle note pour un candidat
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @param request DTO contenant les informations de la note
     * @param recruiter Utilisateur créant la note
     * @return La note créée
     */
    @Transactional
    public RecruiterNoteResponse createNote(UUID jobOpeningId, UUID applicantId, RecruiterNoteRequest request, User recruiter) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        RecruiterNote note = new RecruiterNote();
        note.setApplicant(applicant);
        note.setNoteText(request.getNoteText());
        note.setAuthor(recruiter);
        note.setCreatedAt(Instant.now());
        
        RecruiterNote savedNote = recruiterNoteRepository.save(note);
        return mapToResponse(savedNote);
    }

    /**
     * Met à jour une note existante
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @param noteId ID de la note
     * @param request DTO contenant les informations mises à jour
     * @param recruiter Utilisateur effectuant la mise à jour
     * @return La note mise à jour
     */
    @Transactional
    public RecruiterNoteResponse updateNote(UUID jobOpeningId, UUID applicantId, UUID noteId, RecruiterNoteRequest request, User recruiter) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        RecruiterNote note = recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvée avec l'ID: " + noteId));
        
        // Vérifier que l'utilisateur est l'auteur de la note
        if (!note.getAuthor().getId().equals(recruiter.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas modifier une note dont vous n'êtes pas l'auteur");
        }
        
        note.setNoteText(request.getNoteText());
        
        RecruiterNote updatedNote = recruiterNoteRepository.save(note);
        return mapToResponse(updatedNote);
    }

    /**
     * Supprime une note
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @param noteId ID de la note
     * @param recruiter Utilisateur effectuant la suppression
     */
    @Transactional
    public void deleteNote(UUID jobOpeningId, UUID applicantId, UUID noteId, User recruiter) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        RecruiterNote note = recruiterNoteRepository.findByIdAndApplicant(noteId, applicant)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvée avec l'ID: " + noteId));
        
        // Vérifier que l'utilisateur est l'auteur de la note
        if (!note.getAuthor().getId().equals(recruiter.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas supprimer une note dont vous n'êtes pas l'auteur");
        }
        
        recruiterNoteRepository.delete(note);
    }

    /**
     * Convertit une entité RecruiterNote en DTO RecruiterNoteResponse
     */
    private RecruiterNoteResponse mapToResponse(RecruiterNote note) {
        RecruiterNoteResponse.AuthorSummary authorSummary = null;
        if (note.getAuthor() != null) {
            authorSummary = RecruiterNoteResponse.AuthorSummary.builder()
                    .id(note.getAuthor().getId())
                    .name(note.getAuthor().getName())
                    .email(note.getAuthor().getEmail())
                    .build();
        }
        
        return RecruiterNoteResponse.builder()
                .id(note.getId())
                .applicantId(note.getApplicant().getId())
                .noteText(note.getNoteText())
                .createdAt(note.getCreatedAt())
                .author(authorSummary)
                .build();
    }
}
