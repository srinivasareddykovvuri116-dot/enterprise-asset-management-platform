package com.enterprise.assetmanagement.task;


import java.time.LocalDate;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDate dueDate,
    Long organizationId,
    Long projectId,
    String projectName,
    Long assigneeId,
    String assigneeName
) {}