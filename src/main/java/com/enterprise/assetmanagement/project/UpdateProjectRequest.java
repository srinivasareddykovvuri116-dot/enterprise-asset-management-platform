package com.enterprise.assetmanagement.project;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @Size(max = 150, message = "Project name must not exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        ProjectStatus status,

        Long managerId
) {
}
