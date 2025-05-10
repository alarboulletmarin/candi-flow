package com.candiflow.api.controller;

import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.ExportService;
import com.candiflow.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Contrôleur pour l'export de données
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Exports", description = "API pour l'export de données au format CSV")
public class ExportController {

    private final ExportService exportService;
    private final UserService userService;
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Exporte les candidats d'une offre d'emploi au format CSV
     */
    @Operation(summary = "Exporter les candidats d'une offre", description = "Exporte la liste des candidats d'une offre d'emploi spécifique au format CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export CSV généré avec succès",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi ou utilisateur non trouvé")
    })
    @GetMapping("/job-openings/{jobOpeningId}/applicants")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<InputStreamResource> exportApplicants(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est bien un recruteur
        if (!recruiter.getRole().equals(UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String filename = "applicants_" + jobOpeningId + "_" + 
                LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".csv";
        
        ByteArrayInputStream csvStream = exportService.exportApplicantsToCSV(jobOpeningId, recruiter);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(csvStream));
    }

    /**
     * Exporte toutes les offres d'emploi d'un recruteur au format CSV
     */
    @Operation(summary = "Exporter les offres d'emploi", description = "Exporte la liste des offres d'emploi du recruteur connecté au format CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export CSV généré avec succès",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/job-openings")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<InputStreamResource> exportJobOpenings(
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est bien un recruteur
        if (!recruiter.getRole().equals(UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String filename = "job_openings_" + 
                LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".csv";
        
        ByteArrayInputStream csvStream = exportService.exportJobOpeningsToCSV(recruiter);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(csvStream));
    }

    /**
     * Exporte les statistiques globales au format CSV (admin uniquement)
     */
    @Operation(summary = "Exporter les statistiques globales", description = "Exporte les statistiques globales de la plateforme au format CSV (réservé aux administrateurs)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export CSV généré avec succès",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux administrateurs")
    })
    @GetMapping("/global-stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<InputStreamResource> exportGlobalStats() {
        String filename = "global_stats_" + 
                LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".csv";
        
        ByteArrayInputStream csvStream = exportService.exportGlobalStatsToCSV();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(csvStream));
    }
}
