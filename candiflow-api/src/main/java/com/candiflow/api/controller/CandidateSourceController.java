package com.candiflow.api.controller;

import com.candiflow.api.dto.recruiter.CandidateSourceResponse;
import com.candiflow.api.service.CandidateSourceService;
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
@RequestMapping("/api/candidate-sources")
@RequiredArgsConstructor
@Tag(name = "Sources de candidats", description = "API pour la gestion des sources de candidats (LinkedIn, Indeed, etc.)")
public class CandidateSourceController {

    private final CandidateSourceService candidateSourceService;

    /**
     * Récupère toutes les sources actives
     */
    @Operation(summary = "Récupérer toutes les sources actives", description = "Renvoie la liste de toutes les sources de candidats actives")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des sources récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = CandidateSourceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<List<CandidateSourceResponse>> getAllActiveSources() {
        return ResponseEntity.ok(candidateSourceService.getAllActiveSources());
    }

    /**
     * Récupère une source par son ID
     */
    @Operation(summary = "Récupérer une source par ID", description = "Renvoie les détails d'une source de candidats spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Source trouvée",
                    content = @Content(schema = @Schema(implementation = CandidateSourceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Source non trouvée")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<CandidateSourceResponse> getSourceById(
            @Parameter(description = "ID de la source") @PathVariable UUID id) {
        return candidateSourceService.getSourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
