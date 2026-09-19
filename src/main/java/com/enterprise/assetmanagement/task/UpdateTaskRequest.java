package com.enterprise.assetmanagement.task;


import java.time.LocalDate;

import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
    @Size(max = 200)
    String title,

    @Size(max = 2000)
    String description,

    TaskPriority priority,

    TaskStatus status,

    Long assigneeId,

    LocalDate dueDate
) {}