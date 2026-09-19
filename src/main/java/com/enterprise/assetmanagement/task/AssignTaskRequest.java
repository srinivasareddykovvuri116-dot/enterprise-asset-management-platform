package com.enterprise.assetmanagement.task;

import jakarta.validation.constraints.NotNull;

public record AssignTaskRequest(
        @NotNull
        Long assigneeId
) {
}