package com.candiflow.api.controller;

import com.candiflow.api.dto.application.ApplicationRequest;
import com.candiflow.api.dto.application.ApplicationResponse;
import com.candiflow.api.service.ApplicationService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize; // Désactivé pour permettre l'accès sans authentification
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Contrôleur pour gérer les candidatures
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('ROLE_CANDIDATE')") - Désactivé pour permettre l'accès sans authentification
@Tag(name = "Candidatures", description = "API de gestion des candidatures")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Récupère toutes les candidatures de l'utilisateur connecté
     * @param pageable Pagination
     * @return Page de candidatures
     */
    @Operation(summary = "Récupérer toutes les candidatures", description = "Récupère toutes les candidatures de l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des candidatures récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
            @Parameter(description = "Paramètres de pagination (page, size, sort)") 
            @PageableDefault(size = 10, sort = "dateApplied") Pageable pageable) {
        return ResponseEntity.ok(applicationService.getAllApplications(pageable));
    }

    /**
     * Recherche des candidatures par mot-clé
     * @param keyword Mot-clé de recherche
     * @param pageable Pagination
     * @return Page de candidatures correspondant au mot-clé
     */
    @Operation(summary = "Rechercher des candidatures", description = "Recherche des candidatures par mot-clé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ApplicationResponse>> searchApplications(
            @Parameter(description = "Mot-clé de recherche") @RequestParam String keyword,
            @Parameter(description = "Paramètres de pagination (page, size, sort)") 
            @PageableDefault(size = 10, sort = "dateApplied") Pageable pageable) {
        return ResponseEntity.ok(applicationService.searchApplications(keyword, pageable));
    }

    /**
     * Récupère une candidature par son ID
     * @param id ID de la candidature
     * @return La candidature si elle existe
     */
    @Operation(summary = "Récupérer une candidature par ID", description = "Récupère les détails d'une candidature spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidature trouvée",
                    content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @Parameter(description = "ID de la candidature") @PathVariable UUID id) {
        return applicationService.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle candidature
     * @param request Données de la candidature
     * @return La candidature créée
     */
    @Operation(summary = "Créer une candidature", description = "Crée une nouvelle candidature pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidature créée avec succès",
                    content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de candidature invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Parameter(description = "Données de la candidature") @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.createApplication(request));
    }

    /**
     * Met à jour une candidature existante
     * @param id ID de la candidature
     * @param request Nouvelles données
     * @return La candidature mise à jour
     */
    @Operation(summary = "Mettre à jour une candidature", description = "Met à jour une candidature existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidature mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de candidature invalides"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @Parameter(description = "ID de la candidature") @PathVariable UUID id,
            @Parameter(description = "Nouvelles données de la candidature") @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.updateApplication(id, request));
    }

    /**
     * Supprime une candidature
     * @param id ID de la candidature
     * @return 204 No Content si la suppression a réussi
     */
    @Operation(summary = "Supprimer une candidature", description = "Supprime une candidature existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Candidature supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @Parameter(description = "ID de la candidature") @PathVariable UUID id) {
        boolean deleted = applicationService.deleteApplication(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
