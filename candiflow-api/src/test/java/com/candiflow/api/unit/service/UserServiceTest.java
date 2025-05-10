package com.candiflow.api.unit.service;

import com.candiflow.api.exception.ResourceNotFoundException;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.UserRepository;
import com.candiflow.api.service.UserService;
import com.candiflow.api.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour le UserService
 */
class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEmail = "test@example.com";

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(userEmail);
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(UserRole.CANDIDATE);
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Devrait retourner un utilisateur quand l'email existe")
    void getUserByEmail_WhenEmailExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByEmail(userEmail);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getEmail()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("Devrait retourner Optional vide quand l'email n'existe pas")
    void getUserByEmail_WhenEmailDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Devrait retourner un utilisateur quand l'ID existe")
    void getUserById_WhenIdExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("Devrait lancer ResourceNotFoundException quand l'ID n'existe pas")
    void getUserById_WhenIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Utilisateur non trouvé avec l'ID: " + nonExistentId);
    }
}
