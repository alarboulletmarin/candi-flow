package com.candiflow.api.service;

import com.candiflow.api.dto.recruiter.CandidateSourceResponse;
import com.candiflow.api.model.entity.CandidateSource;
import com.candiflow.api.repository.CandidateSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateSourceService {

    private final CandidateSourceRepository candidateSourceRepository;

    /**
     * Récupère toutes les sources actives
     * @return Liste des sources actives
     */
    @Transactional(readOnly = true)
    public List<CandidateSourceResponse> getAllActiveSources() {
        return candidateSourceRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une source par son ID
     * @param id ID de la source
     * @return La source si elle existe
     */
    @Transactional(readOnly = true)
    public Optional<CandidateSourceResponse> getSourceById(UUID id) {
        return candidateSourceRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Récupère une source par son nom
     * @param name Nom de la source
     * @return La source si elle existe
     */
    @Transactional(readOnly = true)
    public Optional<CandidateSource> getSourceByName(String name) {
        return candidateSourceRepository.findByName(name);
    }

    /**
     * Récupère la source par défaut
     * @return La source par défaut
     */
    @Transactional(readOnly = true)
    public Optional<CandidateSource> getDefaultSource() {
        return candidateSourceRepository.findDefaultSource();
    }

    /**
     * Initialise les sources par défaut si la table est vide
     */
    @Transactional
    public void initializeDefaultSources() {
        if (candidateSourceRepository.count() == 0) {
            // Créer les sources par défaut
            createDefaultSource("LinkedIn", "Candidat trouvé via LinkedIn");
            createDefaultSource("Site Carrière", "Candidat ayant postulé via le site carrière");
            createDefaultSource("Cooptation", "Candidat recommandé par un employé");
            createDefaultSource("JobBoard", "Candidat ayant postulé via un site d'emploi");
            createDefaultSource("Salon/Événement", "Candidat rencontré lors d'un salon ou événement");
            createDefaultSource("Candidature spontanée", "Candidat ayant envoyé une candidature spontanée");
            createDefaultSource("Autre", "Autre source");
        }
    }

    /**
     * Crée une source par défaut
     */
    private CandidateSource createDefaultSource(String name, String description) {
        CandidateSource source = new CandidateSource();
        source.setName(name);
        source.setDescription(description);
        source.setActive(true);
        return candidateSourceRepository.save(source);
    }

    /**
     * Convertit une entité CandidateSource en DTO CandidateSourceResponse
     */
    private CandidateSourceResponse mapToResponse(CandidateSource source) {
        return CandidateSourceResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .description(source.getDescription())
                .isActive(source.isActive())
                .build();
    }
}
