package com.candiflow.api.dto.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO pour la réponse contenant les informations d'un document
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private UUID id;
    private UUID applicationId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Instant uploadedAt;
    private String downloadUrl;
}
