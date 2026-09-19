package com.enterprise.assetmanagement.task;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull
        TaskStatus status
) {
}