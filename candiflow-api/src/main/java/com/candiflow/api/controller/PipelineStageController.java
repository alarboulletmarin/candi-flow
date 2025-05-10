package com.candiflow.api.controller;

import com.candiflow.api.dto.recruiter.PipelineStageResponse;
import com.candiflow.api.service.PipelineStageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipeline-stages")
@RequiredArgsConstructor
@Tag(name = "Étapes du pipeline", description = "API pour la gestion des étapes du pipeline de recrutement")
public class PipelineStageController {

    private final PipelineStageService pipelineStageService;

    /**
     * Récupère toutes les étapes actives du pipeline
     */
    @Operation(summary = "Récupérer toutes les étapes actives", description = "Renvoie la liste de toutes les étapes actives du pipeline de recrutement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des étapes récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = PipelineStageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<List<PipelineStageResponse>> getAllActiveStages() {
        return ResponseEntity.ok(pipelineStageService.getAllActiveStages());
    }

    /**
     * Récupère une étape du pipeline par son ID
     */
    @Operation(summary = "Récupérer une étape par ID", description = "Renvoie les détails d'une étape spécifique du pipeline de recrutement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Étape trouvée",
                    content = @Content(schema = @Schema(implementation = PipelineStageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Étape non trouvée")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<PipelineStageResponse> getStageById(
            @Parameter(description = "ID de l'étape du pipeline") @PathVariable UUID id) {
        return pipelineStageService.getStageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
