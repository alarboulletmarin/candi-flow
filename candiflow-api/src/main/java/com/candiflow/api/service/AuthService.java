package com.candiflow.api.service;

import com.candiflow.api.dto.auth.AuthResponse;
import com.candiflow.api.dto.auth.LoginRequest;
import com.candiflow.api.dto.auth.RegisterRequest;
import com.candiflow.api.dto.auth.UserInfoResponse;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.UserRepository;
import com.candiflow.api.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    
    /**
     * Récupère un utilisateur par défaut (authentification désactivée)
     * @return Un utilisateur par défaut pour toutes les opérations
     */
    public User getCurrentUser() {
        // Rechercher un utilisateur existant ou en créer un par défaut
        return userRepository.findByEmail("utilisateur@defaut.com")
                .orElseGet(() -> {
                    User defaultUser = new User();
                    defaultUser.setEmail("utilisateur@defaut.com");
                    defaultUser.setName("Utilisateur Par Défaut");
                    defaultUser.setPasswordHash(passwordEncoder.encode("password"));
                    defaultUser.setRole(UserRole.CANDIDATE);
                    return userRepository.save(defaultUser);
                });
    }
    
    /**
     * Convertit un utilisateur en DTO UserInfoResponse
     * @param user L'utilisateur à convertir
     * @return Le DTO contenant les informations de l'utilisateur
     */
    public UserInfoResponse getUserInfo(User user) {
        if (user == null) {
            return null;
        }
        
        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // Créer un nouvel utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // Construire le nom complet à partir du prénom et du nom de famille
        String fullName;
        if (request.getFirstName() != null && request.getLastName() != null) {
            fullName = request.getFirstName() + " " + request.getLastName();
        } else if (request.getName() != null) {
            // Fallback sur le champ name pour la rétrocompatibilité
            fullName = request.getName();
        } else {
            fullName = "Utilisateur";
        }
        user.setName(fullName);
        user.setRole(request.getRole());

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        // Générer les tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtTokenUtil.generateToken(userDetails);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        // Construire et retourner la réponse
        return AuthResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role(savedUser.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Authentifier l'utilisateur (lance une exception si les identifiants sont incorrects)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Générer les tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenUtil.generateToken(userDetails);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        // Construire et retourner la réponse
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
