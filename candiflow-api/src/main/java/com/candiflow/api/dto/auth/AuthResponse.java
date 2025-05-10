package com.candiflow.api.dto.auth;

import com.candiflow.api.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private UUID userId;
    private String email;
    private String name;
    private UserRole role;
    private String accessToken;
    private String refreshToken;
}
