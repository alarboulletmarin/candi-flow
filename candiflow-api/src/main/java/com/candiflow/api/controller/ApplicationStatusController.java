package com.candiflow.api.controller;

import com.candiflow.api.dto.application.ApplicationStatusResponse;
import com.candiflow.api.service.ApplicationStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur pour gérer les statuts d'application
 */
@RestController
@RequestMapping("/api/application-statuses")
@RequiredArgsConstructor
@Tag(name = "Statuts de candidature", description = "API pour la gestion des statuts de candidature")
public class ApplicationStatusController {

    private final ApplicationStatusService applicationStatusService;

    /**
     * Récupère tous les statuts actifs
     * @return Liste des statuts actifs
     */
    @Operation(summary = "Récupérer tous les statuts actifs", description = "Renvoie la liste de tous les statuts de candidature actifs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des statuts récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = ApplicationStatusResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ApplicationStatusResponse>> getAllActiveStatuses() {
        return ResponseEntity.ok(applicationStatusService.getAllActiveStatuses());
    }

    /**
     * Récupère un statut par son ID
     * @param id ID du statut
     * @return Le statut s'il existe
     */
    @Operation(summary = "Récupérer un statut par ID", description = "Renvoie les détails d'un statut de candidature spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut trouvé",
                    content = @Content(schema = @Schema(implementation = ApplicationStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Statut non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationStatusResponse> getStatusById(
            @Parameter(description = "ID du statut") @PathVariable UUID id) {
        return applicationStatusService.getStatusById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
