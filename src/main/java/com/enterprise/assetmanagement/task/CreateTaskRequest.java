package com.enterprise.assetmanagement.task;

import java.time.LocalDate;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
    @NotBlank
    @Size(max = 200)
    String title,

    @Size(max = 2000)
    String description,

    @NotNull
    TaskPriority priority,

    @NotNull
    Long projectId,

    Long assigneeId,

    LocalDate dueDate
) {}