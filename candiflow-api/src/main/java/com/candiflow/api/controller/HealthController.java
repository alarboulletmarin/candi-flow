package com.candiflow.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Santé", description = "API pour vérifier l'état de santé de l'application")
public class HealthController {

    /**
     * Vérifie l'état de santé de l'application
     * @return Statut de l'application avec des informations supplémentaires
     */
    @Operation(summary = "Vérifier l'état de santé", description = "Vérifie si l'application est opérationnelle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application opérationnelle",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CandiFlow API");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
