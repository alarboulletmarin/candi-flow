package com.candiflow.api.controller;

import com.candiflow.api.dto.application.StatusUpdateRequest;
import com.candiflow.api.dto.application.StatusUpdateResponse;
import com.candiflow.api.service.StatusUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur pour gérer les mises à jour de statut des candidatures
 */
@RestController
@RequestMapping("/api/applications/{applicationId}/status-updates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_CANDIDATE')")
@Tag(name = "Mises à jour de statut", description = "API pour la gestion des mises à jour de statut des candidatures")
public class StatusUpdateController {

    private final StatusUpdateService statusUpdateService;

    /**
     * Récupère toutes les mises à jour de statut pour une candidature
     * @param applicationId ID de la candidature
     * @return Liste des mises à jour de statut
     */
    @Operation(summary = "Récupérer toutes les mises à jour de statut", description = "Renvoie la liste de toutes les mises à jour de statut pour une candidature spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des mises à jour récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = StatusUpdateResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux candidats"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée")
    })
    @GetMapping
    public ResponseEntity<List<StatusUpdateResponse>> getStatusUpdatesForApplication(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId) {
        return ResponseEntity.ok(statusUpdateService.getStatusUpdatesForApplication(applicationId));
    }

    /**
     * Récupère une mise à jour de statut par son ID
     * @param applicationId ID de la candidature
     * @param statusUpdateId ID de la mise à jour de statut
     * @return La mise à jour de statut si elle existe
     */
    @Operation(summary = "Récupérer une mise à jour de statut par ID", description = "Renvoie les détails d'une mise à jour de statut spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mise à jour de statut trouvée",
                    content = @Content(schema = @Schema(implementation = StatusUpdateResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux candidats"),
            @ApiResponse(responseCode = "404", description = "Candidature ou mise à jour de statut non trouvée")
    })
    @GetMapping("/{statusUpdateId}")
    public ResponseEntity<StatusUpdateResponse> getStatusUpdateById(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "ID de la mise à jour de statut") @PathVariable UUID statusUpdateId) {
        return statusUpdateService.getStatusUpdateById(applicationId, statusUpdateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle mise à jour de statut pour une candidature
     * @param applicationId ID de la candidature
     * @param request Données de la mise à jour de statut
     * @return La mise à jour de statut créée
     */
    @Operation(summary = "Créer une mise à jour de statut", description = "Crée une nouvelle mise à jour de statut pour une candidature spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mise à jour de statut créée avec succès",
                    content = @Content(schema = @Schema(implementation = StatusUpdateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de mise à jour de statut invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux candidats"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée")
    })
    @PostMapping
    public ResponseEntity<StatusUpdateResponse> createStatusUpdate(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "Données de la mise à jour de statut") @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(statusUpdateService.createStatusUpdate(applicationId, request));
    }

    /**
     * Met à jour une mise à jour de statut existante
     * @param applicationId ID de la candidature
     * @param statusUpdateId ID de la mise à jour de statut
     * @param request Nouvelles données
     * @return La mise à jour de statut modifiée
     */
    @Operation(summary = "Mettre à jour une mise à jour de statut", description = "Met à jour une mise à jour de statut existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mise à jour de statut modifiée avec succès",
                    content = @Content(schema = @Schema(implementation = StatusUpdateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de mise à jour de statut invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux candidats"),
            @ApiResponse(responseCode = "404", description = "Candidature ou mise à jour de statut non trouvée")
    })
    @PutMapping("/{statusUpdateId}")
    public ResponseEntity<StatusUpdateResponse> updateStatusUpdate(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "ID de la mise à jour de statut") @PathVariable UUID statusUpdateId,
            @Parameter(description = "Nouvelles données de la mise à jour de statut") @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(statusUpdateService.updateStatusUpdate(applicationId, statusUpdateId, request));
    }

    /**
     * Supprime une mise à jour de statut
     * @param applicationId ID de la candidature
     * @param statusUpdateId ID de la mise à jour de statut
     * @return 204 No Content si la suppression a réussi
     */
    @Operation(summary = "Supprimer une mise à jour de statut", description = "Supprime une mise à jour de statut existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mise à jour de statut supprimée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux candidats"),
            @ApiResponse(responseCode = "404", description = "Candidature ou mise à jour de statut non trouvée")
    })
    @DeleteMapping("/{statusUpdateId}")
    public ResponseEntity<Void> deleteStatusUpdate(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "ID de la mise à jour de statut") @PathVariable UUID statusUpdateId) {
        boolean deleted = statusUpdateService.deleteStatusUpdate(applicationId, statusUpdateId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
