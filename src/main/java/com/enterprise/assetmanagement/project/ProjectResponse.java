package com.enterprise.assetmanagement.project;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        Long organizationId,
        Long managerId,
        String managerName
) {
}