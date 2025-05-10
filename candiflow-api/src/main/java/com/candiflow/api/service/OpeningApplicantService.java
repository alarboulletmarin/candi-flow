package com.candiflow.api.service;

import com.candiflow.api.dto.recruiter.OpeningApplicantRequest;
import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.CandidateSource;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.PipelineStage;
import com.candiflow.api.model.entity.RecruiterNote;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.repository.CandidateSourceRepository;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.repository.PipelineStageRepository;
import com.candiflow.api.repository.RecruiterNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OpeningApplicantService {

    private final OpeningApplicantRepository openingApplicantRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final CandidateSourceRepository candidateSourceRepository;
    private final RecruiterNoteRepository recruiterNoteRepository;
    private final PipelineStageService pipelineStageService;

    /**
     * Récupère tous les candidats pour une offre d'emploi avec pagination
     * @param jobOpeningId ID de l'offre d'emploi
     * @param pageable Informations de pagination
     * @return Page de candidats
     */
    @Transactional(readOnly = true)
    public Page<OpeningApplicantResponse> getAllApplicantsByJobOpening(UUID jobOpeningId, Pageable pageable) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        return openingApplicantRepository.findByJobOpening(jobOpening, pageable)
                .map(this::mapToResponseWithStats);
    }

    /**
     * Recherche des candidats par mot-clé pour une offre d'emploi
     * @param jobOpeningId ID de l'offre d'emploi
     * @param keyword Mot-clé de recherche
     * @param pageable Informations de pagination
     * @return Page de candidats correspondant à la recherche
     */
    @Transactional(readOnly = true)
    public Page<OpeningApplicantResponse> searchApplicantsByKeyword(UUID jobOpeningId, String keyword, Pageable pageable) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        return openingApplicantRepository.searchByJobOpeningAndKeyword(jobOpening, keyword, pageable)
                .map(this::mapToResponseWithStats);
    }

    /**
     * Récupère un candidat par son ID
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @return Le candidat si il existe
     */
    @Transactional(readOnly = true)
    public OpeningApplicantResponse getApplicantById(UUID jobOpeningId, UUID applicantId) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        return mapToResponseWithStats(applicant);
    }

    /**
     * Crée un nouveau candidat pour une offre d'emploi
     * @param jobOpeningId ID de l'offre d'emploi
     * @param request DTO contenant les informations du candidat
     * @param recruiter Utilisateur créant le candidat
     * @return Le candidat créé
     */
    @Transactional
    public OpeningApplicantResponse createApplicant(UUID jobOpeningId, OpeningApplicantRequest request, User recruiter) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        // Vérifier si un candidat avec le même email existe déjà pour cette offre
        if (request.getEmail() != null && openingApplicantRepository.existsByJobOpeningAndEmail(jobOpening, request.getEmail())) {
            throw new IllegalArgumentException("Un candidat avec cet email existe déjà pour cette offre d'emploi");
        }
        
        // Récupérer l'étape du pipeline (par défaut "Nouveau")
        PipelineStage pipelineStage;
        if (request.getPipelineStageId() != null) {
            pipelineStage = pipelineStageRepository.findById(request.getPipelineStageId())
                    .orElseGet(pipelineStageService::getDefaultStage);
        } else {
            pipelineStage = pipelineStageService.getDefaultStage();
        }
        
        // Récupérer la source du candidat (par défaut "Autre")
        CandidateSource source = null;
        if (request.getSourceId() != null) {
            source = candidateSourceRepository.findById(request.getSourceId()).orElse(null);
        }
        if (source == null) {
            source = candidateSourceRepository.findDefaultSource().orElse(null);
        }
        
        // Créer le candidat
        OpeningApplicant applicant = new OpeningApplicant();
        applicant.setJobOpening(jobOpening);
        applicant.setName(request.getName());
        applicant.setEmail(request.getEmail());
        applicant.setPhone(request.getPhone());
        applicant.setCvStoragePath(request.getCvStoragePath());
        applicant.setCoverLetterStoragePath(request.getCoverLetterStoragePath());
        applicant.setApplicationDate(request.getApplicationDate() != null ? request.getApplicationDate() : LocalDate.now());
        applicant.setCurrentStage(pipelineStage);
        applicant.setSource(source);
        // Pas de champ createdBy dans l'entité, on utilise BaseEntity
        applicant.setCreatedAt(Instant.now());
        applicant.setUpdatedAt(Instant.now());
        
        OpeningApplicant savedApplicant = openingApplicantRepository.save(applicant);
        
        // Créer une note initiale si fournie
        if (request.getInitialNotes() != null && !request.getInitialNotes().trim().isEmpty()) {
            RecruiterNote note = new RecruiterNote();
            note.setApplicant(savedApplicant);
            note.setNoteText(request.getInitialNotes());
            note.setAuthor(recruiter);
            note.setCreatedAt(Instant.now());
            recruiterNoteRepository.save(note);
        }
        
        return mapToResponseWithStats(savedApplicant);
    }

    /**
     * Met à jour un candidat existant
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @param request DTO contenant les informations mises à jour
     * @return Le candidat mis à jour
     */
    @Transactional
    public OpeningApplicantResponse updateApplicant(UUID jobOpeningId, UUID applicantId, OpeningApplicantRequest request) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        // Mettre à jour les informations du candidat
        applicant.setName(request.getName());
        
        // Vérifier si l'email a changé et s'il n'existe pas déjà
        if (request.getEmail() != null && !request.getEmail().equals(applicant.getEmail()) && 
                openingApplicantRepository.existsByJobOpeningAndEmail(jobOpening, request.getEmail())) {
            throw new IllegalArgumentException("Un candidat avec cet email existe déjà pour cette offre d'emploi");
        }
        applicant.setEmail(request.getEmail());
        
        applicant.setPhone(request.getPhone());
        applicant.setCvStoragePath(request.getCvStoragePath());
        applicant.setCoverLetterStoragePath(request.getCoverLetterStoragePath());
        
        if (request.getApplicationDate() != null) {
            applicant.setApplicationDate(request.getApplicationDate());
        }
        
        // Mettre à jour l'étape du pipeline si spécifiée
        if (request.getPipelineStageId() != null) {
            PipelineStage pipelineStage = pipelineStageRepository.findById(request.getPipelineStageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Étape du pipeline non trouvée avec l'ID: " + request.getPipelineStageId()));
            applicant.setCurrentStage(pipelineStage);
        }
        
        // Mettre à jour la source si spécifiée
        if (request.getSourceId() != null) {
            CandidateSource source = candidateSourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source non trouvée avec l'ID: " + request.getSourceId()));
            applicant.setSource(source);
        }
        
        applicant.setUpdatedAt(Instant.now());
        
        OpeningApplicant updatedApplicant = openingApplicantRepository.save(applicant);
        return mapToResponseWithStats(updatedApplicant);
    }

    /**
     * Met à jour l'étape du pipeline d'un candidat
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     * @param stageId ID de la nouvelle étape
     * @param recruiter Utilisateur effectuant la mise à jour
     * @param note Note optionnelle expliquant le changement d'étape
     * @return Le candidat mis à jour
     */
    @Transactional
    public OpeningApplicantResponse updateApplicantStage(UUID jobOpeningId, UUID applicantId, UUID stageId, User recruiter, String note) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        PipelineStage newStage = pipelineStageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Étape du pipeline non trouvée avec l'ID: " + stageId));
        
        // Mettre à jour l'étape
        applicant.setCurrentStage(newStage);
        applicant.setUpdatedAt(Instant.now());
        
        // Ajouter une note si fournie
        if (note != null && !note.trim().isEmpty()) {
            RecruiterNote recruiterNote = new RecruiterNote();
            recruiterNote.setApplicant(applicant);
            recruiterNote.setNoteText("Changement d'étape: " + newStage.getName() + "\n" + note);
            recruiterNote.setAuthor(recruiter);
            recruiterNote.setCreatedAt(Instant.now());
            recruiterNoteRepository.save(recruiterNote);
        }
        
        OpeningApplicant updatedApplicant = openingApplicantRepository.save(applicant);
        return mapToResponseWithStats(updatedApplicant);
    }

    /**
     * Supprime un candidat
     * @param jobOpeningId ID de l'offre d'emploi
     * @param applicantId ID du candidat
     */
    @Transactional
    public void deleteApplicant(UUID jobOpeningId, UUID applicantId) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + jobOpeningId));
        
        OpeningApplicant applicant = openingApplicantRepository.findByIdAndJobOpening(applicantId, jobOpening)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat non trouvé avec l'ID: " + applicantId));
        
        // Supprimer le candidat et toutes ses notes (cascade)
        openingApplicantRepository.delete(applicant);
    }

    /**
     * Convertit une entité OpeningApplicant en DTO OpeningApplicantResponse
     */
    private OpeningApplicantResponse mapToResponse(OpeningApplicant applicant) {
        OpeningApplicantResponse.PipelineStageSummary stageSummary = null;
        if (applicant.getCurrentStage() != null) {
            stageSummary = OpeningApplicantResponse.PipelineStageSummary.builder()
                    .id(applicant.getCurrentStage().getId())
                    .name(applicant.getCurrentStage().getName())
                    .isEndStage(applicant.getCurrentStage().isEndStage())
                    .build();
        }
        
        OpeningApplicantResponse.CandidateSourceSummary sourceSummary = null;
        if (applicant.getSource() != null) {
            sourceSummary = OpeningApplicantResponse.CandidateSourceSummary.builder()
                    .id(applicant.getSource().getId())
                    .name(applicant.getSource().getName())
                    .build();
        }
        
        return OpeningApplicantResponse.builder()
                .id(applicant.getId())
                .jobOpeningId(applicant.getJobOpening().getId())
                .name(applicant.getName())
                .email(applicant.getEmail())
                .phone(applicant.getPhone())
                .cvStoragePath(applicant.getCvStoragePath())
                .coverLetterStoragePath(applicant.getCoverLetterStoragePath())
                .applicationDate(applicant.getApplicationDate())
                .createdAt(applicant.getCreatedAt())
                .updatedAt(applicant.getUpdatedAt())
                .pipelineStage(stageSummary)
                .source(sourceSummary)
                .build();
    }

    /**
     * Convertit une entité OpeningApplicant en DTO OpeningApplicantResponse avec statistiques
     */
    private OpeningApplicantResponse mapToResponseWithStats(OpeningApplicant applicant) {
        OpeningApplicantResponse response = mapToResponse(applicant);
        
        // Ajouter les statistiques
        int totalNotes = (int) recruiterNoteRepository.countByApplicant(applicant);
        response.setTotalNotes(totalNotes);
        
        return response;
    }
}
