package com.candiflow.api.controller;

import com.candiflow.api.dto.recruiter.RecruiterNoteRequest;
import com.candiflow.api.dto.recruiter.RecruiterNoteResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.service.RecruiterNoteService;
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
@RequestMapping("/api/job-openings/{jobOpeningId}/applicants/{applicantId}/notes")
@RequiredArgsConstructor
@Tag(name = "Notes de recruteurs", description = "API pour la gestion des notes de recruteurs sur les candidats")
public class RecruiterNoteController {

    private final RecruiterNoteService recruiterNoteService;
    private final UserService userService;

    /**
     * Récupère toutes les notes pour un candidat avec pagination
     */
    @Operation(summary = "Récupérer toutes les notes", description = "Renvoie la liste paginée de toutes les notes pour un candidat spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des notes récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi ou candidat non trouvé")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Page<RecruiterNoteResponse>> getAllNotesByApplicant(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId,
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {
        
        return ResponseEntity.ok(recruiterNoteService.getAllNotesByApplicant(jobOpeningId, applicantId, pageable));
    }

    /**
     * Récupère toutes les notes pour un candidat triées par date de création (descendante)
     */
    @Operation(summary = "Récupérer la timeline des notes", description = "Renvoie toutes les notes pour un candidat triées par date de création (descendante)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timeline des notes récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = RecruiterNoteResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi ou candidat non trouvé")
    })
    @GetMapping("/timeline")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<List<RecruiterNoteResponse>> getAllNotesByApplicantSortedByDate(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId) {
        
        return ResponseEntity.ok(recruiterNoteService.getAllNotesByApplicantSortedByDate(jobOpeningId, applicantId));
    }

    /**
     * Récupère une note par son ID
     */
    @Operation(summary = "Récupérer une note par ID", description = "Renvoie les détails d'une note spécifique pour un candidat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note trouvée",
                    content = @Content(schema = @Schema(implementation = RecruiterNoteResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi, candidat ou note non trouvé")
    })
    @GetMapping("/{noteId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<RecruiterNoteResponse> getNoteById(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId,
            @Parameter(description = "ID de la note") @PathVariable UUID noteId) {
        
        return ResponseEntity.ok(recruiterNoteService.getNoteById(jobOpeningId, applicantId, noteId));
    }

    /**
     * Crée une nouvelle note pour un candidat
     */
    @Operation(summary = "Créer une note", description = "Crée une nouvelle note pour un candidat spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Note créée avec succès",
                    content = @Content(schema = @Schema(implementation = RecruiterNoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de note invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi, candidat ou utilisateur non trouvé")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<RecruiterNoteResponse> createNote(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId,
            @Parameter(description = "Données de la note") @Valid @RequestBody RecruiterNoteRequest request,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est bien un recruteur
        if (!recruiter.getRole().equals(UserRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        RecruiterNoteResponse response = recruiterNoteService.createNote(jobOpeningId, applicantId, request, recruiter);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour une note existante
     */
    @Operation(summary = "Mettre à jour une note", description = "Met à jour une note existante pour un candidat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = RecruiterNoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de note invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs ou utilisateur non auteur de la note"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi, candidat, note ou utilisateur non trouvé")
    })
    @PutMapping("/{noteId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<RecruiterNoteResponse> updateNote(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId,
            @Parameter(description = "ID de la note") @PathVariable UUID noteId,
            @Parameter(description = "Nouvelles données de la note") @Valid @RequestBody RecruiterNoteRequest request,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        try {
            RecruiterNoteResponse response = recruiterNoteService.updateNote(
                    jobOpeningId, applicantId, noteId, request, recruiter);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // L'utilisateur n'est pas l'auteur de la note
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Supprime une note
     */
    @Operation(summary = "Supprimer une note", description = "Supprime une note existante pour un candidat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Note supprimée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs ou utilisateur non auteur de la note"),
            @ApiResponse(responseCode = "404", description = "Offre d'emploi, candidat, note ou utilisateur non trouvé")
    })
    @DeleteMapping("/{noteId}")
    @PreAuthorize("hasAuthority('RECRUITER')")
    public ResponseEntity<Void> deleteNote(
            @Parameter(description = "ID de l'offre d'emploi") @PathVariable UUID jobOpeningId,
            @Parameter(description = "ID du candidat") @PathVariable UUID applicantId,
            @Parameter(description = "ID de la note") @PathVariable UUID noteId,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User recruiter = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        try {
            recruiterNoteService.deleteNote(jobOpeningId, applicantId, noteId, recruiter);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // L'utilisateur n'est pas l'auteur de la note
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
