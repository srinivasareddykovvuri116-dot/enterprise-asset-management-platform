package com.enterprise.assetmanagement.security;

public record AuthResponse(
        String token,
        Long userId,
        Long organizationId,
        String role
) {
}