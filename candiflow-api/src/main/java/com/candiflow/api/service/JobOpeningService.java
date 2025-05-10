package com.candiflow.api.service;

import com.candiflow.api.dto.recruiter.JobOpeningRequest;
import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.PipelineStage;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
import com.candiflow.api.observer.EventManager;
import com.candiflow.api.observer.JobOpeningEvent;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.repository.PipelineStageRepository;
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
public class JobOpeningService {

    private final JobOpeningRepository jobOpeningRepository;
    private final OpeningApplicantRepository openingApplicantRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final EventManager eventManager;

    /**
     * Récupère toutes les offres d'emploi avec pagination
     * @param pageable Informations de pagination
     * @return Page d'offres d'emploi
     */
    @Transactional(readOnly = true)
    public Page<JobOpeningResponse> getAllJobOpenings(Pageable pageable) {
        return jobOpeningRepository.findAll(pageable)
                .map(this::mapToResponseWithStats);
    }

    /**
     * Récupère toutes les offres d'emploi actives
     * @return Liste des offres d'emploi actives
     */
    @Transactional(readOnly = true)
    public List<JobOpeningResponse> getAllActiveJobOpenings() {
        return jobOpeningRepository.findByStatus(JobStatus.OPEN).stream()
                .map(this::mapToResponseWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une offre d'emploi par son ID
     * @param id ID de l'offre d'emploi
     * @return L'offre d'emploi si elle existe
     */
    @Transactional(readOnly = true)
    public JobOpeningResponse getJobOpeningById(UUID id) {
        JobOpening jobOpening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + id));
        return mapToResponseWithStats(jobOpening);
    }

    /**
     * Crée une nouvelle offre d'emploi
     * @param request DTO contenant les informations de l'offre d'emploi
     * @param recruiter Utilisateur créant l'offre d'emploi
     * @return L'offre d'emploi créée
     */
    @Transactional
    public JobOpeningResponse createJobOpening(JobOpeningRequest request, User recruiter) {
        JobOpening jobOpening = new JobOpening();
        jobOpening.setTitle(request.getTitle());
        jobOpening.setDescription(request.getDescription());
        jobOpening.setStatus(request.getStatus());
        jobOpening.setRecruiter(recruiter);
        jobOpening.setCreatedAt(Instant.now());
        jobOpening.setUpdatedAt(Instant.now());
        
        JobOpening savedJobOpening = jobOpeningRepository.save(jobOpening);
        
        // Déclencher un événement de création d'offre d'emploi
        eventManager.fireEvent(JobOpeningEvent.created(this.getClass().getSimpleName(), savedJobOpening));
        
        return mapToResponse(savedJobOpening);
    }

    /**
     * Met à jour une offre d'emploi existante
     * @param id ID de l'offre d'emploi
     * @param request DTO contenant les informations mises à jour
     * @return L'offre d'emploi mise à jour
     */
    @Transactional
    public JobOpeningResponse updateJobOpening(UUID id, JobOpeningRequest request) {
        JobOpening jobOpening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + id));
        
        jobOpening.setTitle(request.getTitle());
        jobOpening.setDescription(request.getDescription());
        jobOpening.setStatus(request.getStatus());
        jobOpening.setUpdatedAt(Instant.now());
        
        JobOpening updatedJobOpening = jobOpeningRepository.save(jobOpening);
        
        // Déclencher un événement de mise à jour d'offre d'emploi
        eventManager.fireEvent(JobOpeningEvent.updated(this.getClass().getSimpleName(), updatedJobOpening));
        
        return mapToResponseWithStats(updatedJobOpening);
    }

    /**
     * Supprime une offre d'emploi
     * @param id ID de l'offre d'emploi
     */
    @Transactional
    public void deleteJobOpening(UUID id) {
        JobOpening jobOpening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre d'emploi non trouvée avec l'ID: " + id));
        
        // Vérifier s'il y a des candidats associés à cette offre
        long applicantCount = openingApplicantRepository.countByJobOpening(jobOpening);
        if (applicantCount > 0) {
            // Si des candidats existent, on marque l'offre comme fermée au lieu de la supprimer
            jobOpening.setStatus(JobStatus.CLOSED);
            jobOpening.setUpdatedAt(Instant.now());
            JobOpening closedJobOpening = jobOpeningRepository.save(jobOpening);
            
            // Déclencher un événement de mise à jour d'offre d'emploi
            eventManager.fireEvent(JobOpeningEvent.updated(this.getClass().getSimpleName(), closedJobOpening));
        } else {
            // Sinon, on peut la supprimer
            // Déclencher un événement de suppression d'offre d'emploi avant de supprimer
            eventManager.fireEvent(JobOpeningEvent.deleted(this.getClass().getSimpleName(), jobOpening));
            
            jobOpeningRepository.delete(jobOpening);
        }
    }

    /**
     * Convertit une entité JobOpening en DTO JobOpeningResponse
     */
    private JobOpeningResponse mapToResponse(JobOpening jobOpening) {
        return JobOpeningResponse.builder()
                .id(jobOpening.getId())
                .title(jobOpening.getTitle())
                .description(jobOpening.getDescription())
                .status(jobOpening.getStatus())
                .createdAt(jobOpening.getCreatedAt())
                .updatedAt(jobOpening.getUpdatedAt())
                .build();
    }

    /**
     * Convertit une entité JobOpening en DTO JobOpeningResponse avec statistiques
     */
    private JobOpeningResponse mapToResponseWithStats(JobOpening jobOpening) {
        JobOpeningResponse response = mapToResponse(jobOpening);
        
        // Ajouter les statistiques
        int totalApplicants = (int) openingApplicantRepository.countByJobOpening(jobOpening);
        response.setTotalApplicants(totalApplicants);
        
        // Trouver l'étape "Nouveau" pour compter les nouveaux candidats
        pipelineStageRepository.findByName("Nouveau").ifPresent(newStage -> {
            int newApplicants = (int) openingApplicantRepository.countByJobOpeningAndCurrentStage(jobOpening, newStage);
            response.setNewApplicants(newApplicants);
        });
        
        // Compter les candidats en cours de processus (ni nouveaux, ni en étape finale)
        List<PipelineStage> endStages = pipelineStageRepository.findByIsEndStageTrue();
        int endStageApplicants = 0;
        for (PipelineStage endStage : endStages) {
            endStageApplicants += openingApplicantRepository.countByJobOpeningAndCurrentStage(jobOpening, endStage);
        }
        response.setEndStageApplicants(endStageApplicants);
        
        // Les candidats en processus = total - (nouveaux + étapes finales)
        int inProcessApplicants = totalApplicants - (response.getNewApplicants() + endStageApplicants);
        response.setInProcessApplicants(inProcessApplicants);
        
        return response;
    }
}
