package com.candiflow.api.controller;

import com.candiflow.api.dto.auth.AuthResponse;
import com.candiflow.api.dto.auth.LoginRequest;
import com.candiflow.api.dto.auth.RegisterRequest;
import com.candiflow.api.dto.auth.UserInfoResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "API d'authentification et de gestion des utilisateurs")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Inscription d'un nouvel utilisateur", description = "Permet de créer un nouveau compte utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscription réussie", 
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données d'inscription invalides"),
            @ApiResponse(responseCode = "409", description = "L'email est déjà utilisé")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur et renvoie un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie", 
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données de connexion invalides"),
            @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Obtenir les informations de l'utilisateur connecté", description = "Renvoie les informations de l'utilisateur authentifié")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informations utilisateur récupérées avec succès", 
                    content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    })
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getUserInfo(currentUser));
    }
}
