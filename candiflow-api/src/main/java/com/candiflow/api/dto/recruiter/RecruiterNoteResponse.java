package com.candiflow.api.dto.recruiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'une note de recruteur
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterNoteResponse {

    private UUID id;
    private UUID applicantId;
    private String noteText;
    private Instant createdAt;
    
    // Informations sur l'auteur
    private AuthorSummary author;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorSummary {
        private UUID id;
        private String name;
        private String email;
    }
}
