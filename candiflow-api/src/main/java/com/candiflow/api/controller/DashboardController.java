package com.candiflow.api.controller;

import com.candiflow.api.dto.dashboard.GlobalStatsResponse;
import com.candiflow.api.dto.dashboard.JobOpeningStatsResponse;
import com.candiflow.api.dto.dashboard.RecruiterDashboardResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.DashboardService;
import com.candiflow.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Contrôleur pour les tableaux de bord et statistiques
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Tableaux de bord", description = "API pour les tableaux de bord et statistiques")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    /**
     * Récupère le tableau de bord du recruteur connecté
     */
    @Operation(summary = "Tableau de bord recruteur", description = "Récupère les statistiques et données du tableau de bord pour le recruteur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tableau de bord récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = RecruiterDashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/recruiter")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<RecruiterDashboardResponse> getRecruiterDashboard(
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est bien un recruteur
        if (!recruiter.getRole().equals(UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(dashboardService.getRecruiterDashboard(recruiter));
    }

    /**
     * Récupère les statistiques pour une offre d'emploi spécifique
     */
    @Operation(summary = "Statistiques d'une offre d'emploi", description = "Récupère les statistiques détaillées pour une offre d'emploi spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = JobOpeningStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi non trouvée")
    })
    @GetMapping("/job-openings/{jobOpeningId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<JobOpeningStatsResponse> getJobOpeningStats(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId) {
        
        return ResponseEntity.ok(dashboardService.getJobOpeningStats(jobOpeningId));
    }

    /**
     * Récupère les statistiques globales (admin uniquement)
     */
    @Operation(summary = "Statistiques globales", description = "Récupère les statistiques globales de la plateforme (réservé aux administrateurs)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques globales récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = GlobalStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux administrateurs")
    })
    @GetMapping("/global")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
        return ResponseEntity.ok(dashboardService.getGlobalStats());
    }
}
