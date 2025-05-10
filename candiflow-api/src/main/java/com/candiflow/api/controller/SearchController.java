package com.candiflow.api.controller;

import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrôleur pour la recherche avancée
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Recherche", description = "API de recherche avancée pour les offres d'emploi et les candidats")
public class SearchController {

    private final SearchService searchService;

    /**
     * Recherche avancée d'offres d'emploi
     */
    @Operation(summary = "Rechercher des offres d'emploi", description = "Recherche avancée d'offres d'emploi avec filtres multiples")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs")
    })
    @GetMapping("/job-openings")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Page<JobOpeningResponse>> searchJobOpenings(
            @Parameter(description = "Titre de l'offre d'emploi (recherche partielle)") @RequestParam(required = false) String title,
            @Parameter(description = "Statut de l'offre (OPEN, CLOSED, DRAFT)") @RequestParam(required = false) String status,
            @Parameter(description = "Date de création minimale (format ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter,
            @Parameter(description = "Date de création maximale (format ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdBefore,
            @Parameter(description = "ID du recruteur") @RequestParam(required = false) UUID recruiterId,
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {

        return ResponseEntity.ok(searchService.searchJobOpenings(
                title, status, createdAfter, createdBefore, recruiterId, pageable));
    }

    /**
     * Recherche avancée de candidats
     */
    @Operation(summary = "Rechercher des candidats", description = "Recherche avancée de candidats avec filtres multiples")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs")
    })
    @GetMapping("/applicants")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Page<OpeningApplicantResponse>> searchApplicants(
            @Parameter(description = "ID de l'offre d'emploi") @RequestParam(required = false) UUID jobOpeningId,
            @Parameter(description = "Nom du candidat (recherche partielle)") @RequestParam(required = false) String name,
            @Parameter(description = "Email du candidat (recherche partielle)") @RequestParam(required = false) String email,
            @Parameter(description = "Téléphone du candidat (recherche partielle)") @RequestParam(required = false) String phone,
            @Parameter(description = "ID de l'étape du pipeline") @RequestParam(required = false) UUID stageId,
            @Parameter(description = "ID de la source du candidat") @RequestParam(required = false) UUID sourceId,
            @Parameter(description = "Date de candidature minimale (format ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate applicationDateAfter,
            @Parameter(description = "Date de candidature maximale (format ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate applicationDateBefore,
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {

        return ResponseEntity.ok(searchService.searchApplicants(
                jobOpeningId, name, email, phone, stageId, sourceId, 
                applicationDateAfter, applicationDateBefore, pageable));
    }
}
