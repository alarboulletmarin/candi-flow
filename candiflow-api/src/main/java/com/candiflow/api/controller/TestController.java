package com.candiflow.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de test pour vérifier les autorisations et l'authentification
 * Ce contrôleur sera supprimé une fois que l'authentification aura été testée
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "Tests", description = "Endpoints de test pour vérifier les autorisations et l'authentification")
public class TestController {

    @Operation(summary = "Test d'accès pour tous les utilisateurs", description = "Vérifie l'accès pour tous les utilisateurs authentifiés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accès autorisé",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé - Authentification requise")
    })
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> testAllAccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Accès autorisé pour tous les utilisateurs authentifiés");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Test d'accès pour les candidats", description = "Vérifie l'accès pour les utilisateurs avec le rôle CANDIDATE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accès autorisé",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé - Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux candidats")
    })
    @GetMapping("/candidate")
    @PreAuthorize("hasRole('ROLE_CANDIDATE')")
    public ResponseEntity<Map<String, Object>> testCandidateAccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Accès autorisé pour les candidats uniquement");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Test d'accès pour les recruteurs", description = "Vérifie l'accès pour les utilisateurs avec le rôle RECRUITER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accès autorisé",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé - Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Accès interdit - Réservé aux recruteurs")
    })
    @GetMapping("/recruiter")
    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    public ResponseEntity<Map<String, Object>> testRecruiterAccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Accès autorisé pour les recruteurs uniquement");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
