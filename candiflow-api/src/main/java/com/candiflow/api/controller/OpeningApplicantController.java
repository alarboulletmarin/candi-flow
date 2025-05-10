package com.candiflow.api.controller;

import com.candiflow.api.dto.recruiter.OpeningApplicantRequest;
import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.OpeningApplicantService;
import com.candiflow.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/job-openings/{jobOpeningId}/applicants")
@RequiredArgsConstructor
@Tag(name = "Candidats", description = "API pour la gestion des candidats associés aux offres d'emploi")
public class OpeningApplicantController {

    private final OpeningApplicantService openingApplicantService;
    private final UserService userService;

    /**
     * Récupère tous les candidats pour une offre d'emploi avec pagination
     */
    @Operation(summary = "Récupérer tous les candidats", description = "Renvoie la liste paginée de tous les candidats pour une offre d'emploi spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des candidats récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi non trouvée")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Page<OpeningApplicantResponse>> getAllApplicantsByJobOpening(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {
        
        return ResponseEntity.ok(openingApplicantService.getAllApplicantsByJobOpening(jobOpeningId, pageable));
    }

    /**
     * Recherche des candidats par mot-clé pour une offre d'emploi
     */
    @Operation(summary = "Rechercher des candidats", description = "Recherche des candidats par mot-clé pour une offre d'emploi spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi non trouvée")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Page<OpeningApplicantResponse>> searchApplicantsByKeyword(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "Mot-clé de recherche") @RequestParam String keyword,
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {
        
        return ResponseEntity.ok(openingApplicantService.searchApplicantsByKeyword(jobOpeningId, keyword, pageable));
    }

    /**
     * Récupère un candidat par son ID
     */
    @Operation(summary = "Récupérer un candidat par ID", description = "Renvoie les détails d'un candidat spécifique pour une offre d'emploi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidat trouvé",
                    content = @Content(schema = @Schema(implementation = OpeningApplicantResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi ou candidat non trouvé")
    })
    @GetMapping("/{applicantId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<OpeningApplicantResponse> getApplicantById(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId) {
        
        return ResponseEntity.ok(openingApplicantService.getApplicantById(jobOpeningId, applicantId));
    }

    /**
     * Crée un nouveau candidat pour une offre d'emploi
     */
    @Operation(summary = "Créer un candidat", description = "Crée un nouveau candidat pour une offre d'emploi spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Candidat créé avec succès",
                    content = @Content(schema = @Schema(implementation = OpeningApplicantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de candidat invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi ou utilisateur non trouvé")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<OpeningApplicantResponse> createApplicant(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "Données du candidat") @Valid @RequestBody OpeningApplicantRequest request,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est bien un recruteur
        if (!recruiter.getRole().equals(UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        OpeningApplicantResponse response = openingApplicantService.createApplicant(jobOpeningId, request, recruiter);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour un candidat existant
     */
    @Operation(summary = "Mettre à jour un candidat", description = "Met à jour un candidat existant pour une offre d'emploi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidat mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = OpeningApplicantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de candidat invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi ou candidat non trouvé")
    })
    @PutMapping("/{applicantId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<OpeningApplicantResponse> updateApplicant(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId,
            @Parameter(description = "Nouvelles données du candidat") @Valid @RequestBody OpeningApplicantRequest request) {
        
        OpeningApplicantResponse response = openingApplicantService.updateApplicant(jobOpeningId, applicantId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour l'étape du pipeline d'un candidat
     */
    @Operation(summary = "Mettre à jour l'étape du candidat", description = "Déplace un candidat vers une nouvelle étape du pipeline de recrutement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Étape du candidat mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = OpeningApplicantResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi, candidat, étape ou utilisateur non trouvé")
    })
    @PutMapping("/{applicantId}/stage/{stageId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<OpeningApplicantResponse> updateApplicantStage(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId,
            @Parameter(description = "ID de la nouvelle étape") @PathVariable UUID stageId,
            @Parameter(description = "Note optionnelle sur le changement d'étape") @RequestParam(required = false) String note,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est bien un recruteur
        if (!recruiter.getRole().equals(UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        OpeningApplicantResponse response = openingApplicantService.updateApplicantStage(
                jobOpeningId, applicantId, stageId, recruiter, note);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un candidat
     */
    @Operation(summary = "Supprimer un candidat", description = "Supprime un candidat existant d'une offre d'emploi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Candidat supprimé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi ou candidat non trouvé")
    })
    @DeleteMapping("/{applicantId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Void> deleteApplicant(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId) {
        
        openingApplicantService.deleteApplicant(jobOpeningId, applicantId);
        return ResponseEntity.noContent().build();
    }
}
