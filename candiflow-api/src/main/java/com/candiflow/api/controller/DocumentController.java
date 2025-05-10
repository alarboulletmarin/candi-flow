package com.candiflow.api.controller;

import com.candiflow.api.dto.candidate.DocumentResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.service.DocumentService;
import com.candiflow.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

@RestController
@RequestMapping("/api/applications/{applicationId}/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "API pour la gestion des documents liés aux candidatures")
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;

    /**
     * Récupère tous les documents pour une candidature avec pagination
     */
    @Operation(summary = "Récupérer tous les documents", description = "Renvoie la liste paginée de tous les documents pour une candidature spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des documents récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DocumentResponse>> getAllDocumentsByApplication(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {
        
        return ResponseEntity.ok(documentService.getAllDocumentsByApplication(applicationId, pageable));
    }

    /**
     * Récupère un document par son ID
     */
    @Operation(summary = "Récupérer un document par ID", description = "Renvoie les détails d'un document spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document trouvé",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit"),
            @ApiResponse(responseCode = "404", description = "Candidature ou document non trouvé")
    })
    @GetMapping("/{documentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "ID du document") @PathVariable UUID documentId) {
        
        return ResponseEntity.ok(documentService.getDocumentById(applicationId, documentId));
    }

    /**
     * Télécharge un document
     */
    @Operation(summary = "Télécharger un document", description = "Télécharge un document spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document téléchargé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit"),
            @ApiResponse(responseCode = "404", description = "Candidature ou document non trouvé")
    })
    @GetMapping("/{documentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadDocument(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "ID du document") @PathVariable UUID documentId) {
        
        Resource resource = documentService.downloadDocument(applicationId, documentId);
        DocumentResponse document = documentService.getDocumentById(applicationId, documentId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Téléverse un nouveau document pour une candidature
     */
    @Operation(summary = "Téléverser un document", description = "Téléverse un nouveau document pour une candidature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document téléversé avec succès",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou manquant"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Vous n'êtes pas le propriétaire de cette candidature"),
            @ApiResponse(responseCode = "404", description = "Candidature non trouvée")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "Fichier à téléverser") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        try {
            DocumentResponse response = documentService.uploadDocument(applicationId, file, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Supprime un document
     */
    @Operation(summary = "Supprimer un document", description = "Supprime un document existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document supprimé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Vous n'êtes pas le propriétaire de cette candidature"),
            @ApiResponse(responseCode = "404", description = "Candidature ou document non trouvé")
    })
    @DeleteMapping("/{documentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID de la candidature") @PathVariable UUID applicationId,
            @Parameter(description = "ID du document") @PathVariable UUID documentId,
            @Parameter(description = "Détails de l'utilisateur authentifié") @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        
        try {
            documentService.deleteDocument(applicationId, documentId, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
