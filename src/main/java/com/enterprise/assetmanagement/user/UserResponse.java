package com.enterprise.assetmanagement.user;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        UserRole role,
        Long organizationId,
        boolean active
) {
}