package com.candiflow.api.service;

import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Récupère un utilisateur par son email
     * @param email Email de l'utilisateur
     * @return L'utilisateur si trouvé
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Récupère un utilisateur par son ID
     * @param id ID de l'utilisateur
     * @return L'utilisateur si trouvé
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }
}
