package com.enterprise.assetmanagement.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long organizationId,
        Long actorId,
        String actorName,
        AuditAction action,
        String entityType,
        Long entityId,
        String details,
        LocalDateTime createdAt
) {
}