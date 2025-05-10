package com.candiflow.api.controller;

import com.candiflow.api.dto.recruiter.JobOpeningRequest;
import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.JobOpeningService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-openings")
@RequiredArgsConstructor
@Tag(name = "Offres d'emploi", description = "API pour la gestion des offres d'emploi")
public class JobOpeningController {

    private final JobOpeningService jobOpeningService;
    private final UserService userService;

    /**
     * Récupère toutes les offres d'emploi avec pagination
     */
    @Operation(summary = "Récupérer toutes les offres d'emploi", description = "Renvoie la liste paginée de toutes les offres d'emploi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des offres récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Page<JobOpeningResponse>> getAllJobOpenings(
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {
        return ResponseEntity.ok(jobOpeningService.getAllJobOpenings(pageable));
    }

    /**
     * Récupère toutes les offres d'emploi actives
     */
    @Operation(summary = "Récupérer les offres d'emploi actives", description = "Renvoie la liste de toutes les offres d'emploi actives (statut OPEN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des offres actives récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = JobOpeningResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs")
    })
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<List<JobOpeningResponse>> getAllActiveJobOpenings() {
        return ResponseEntity.ok(jobOpeningService.getAllActiveJobOpenings());
    }

    /**
     * Récupère une offre d'emploi par son ID
     */
    @Operation(summary = "Récupérer une offre d'emploi par ID", description = "Renvoie les détails d'une offre d'emploi spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offre d'emploi trouvée",
                    content = @Content(schema = @Schema(implementation = JobOpeningResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi non trouvée")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<JobOpeningResponse> getJobOpeningById(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID id) {
        return ResponseEntity.ok(jobOpeningService.getJobOpeningById(id));
    }

    /**
     * Crée une nouvelle offre d'emploi
     */
    @Operation(summary = "Créer une offre d'emploi", description = "Crée une nouvelle offre d'emploi pour le recruteur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Offre d'emploi créée avec succès",
                    content = @Content(schema = @Schema(implementation = JobOpeningResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données d'offre d'emploi invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<JobOpeningResponse> createJobOpening(
            @Parameter(description = "Données de l'offre d'emploi") @Valid @RequestBody JobOpeningRequest request,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est bien un recruteur
        if (!recruiter.getRole().equals(UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        JobOpeningResponse response = jobOpeningService.createJobOpening(request, recruiter);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour une offre d'emploi existante
     */
    @Operation(summary = "Mettre à jour une offre d'emploi", description = "Met à jour une offre d'emploi existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offre d'emploi mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = JobOpeningResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données d'offre d'emploi invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi non trouvée")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<JobOpeningResponse> updateJobOpening(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID id,
            @Parameter(description = "Nouvelles données de l'offre d'emploi") @Valid @RequestBody JobOpeningRequest request) {
        
        JobOpeningResponse response = jobOpeningService.updateJobOpening(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime une offre d'emploi
     */
    @Operation(summary = "Supprimer une offre d'emploi", description = "Supprime une offre d'emploi existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Offre d'emploi supprimée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi non trouvée")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Void> deleteJobOpening(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID id) {
        jobOpeningService.deleteJobOpening(id);
        return ResponseEntity.noContent().build();
    }
}
