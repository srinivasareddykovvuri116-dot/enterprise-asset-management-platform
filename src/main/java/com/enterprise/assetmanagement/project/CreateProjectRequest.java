package com.enterprise.assetmanagement.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        Long managerId
) {
}