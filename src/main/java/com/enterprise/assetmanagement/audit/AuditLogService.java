package com.enterprise.assetmanagement.audit;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    // ============================================================
    // ALL ORGANIZATION AUDIT LOGS
    // ============================================================

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getOrganizationAuditLogs(
            Long organizationId) {

        return auditLogRepository
                .findByOrganizationIdOrderByCreatedAtDesc(
                        organizationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // FILTER BY ACTION
    // ============================================================

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getOrganizationAuditLogsByAction(
            Long organizationId,
            AuditAction action) {

        return auditLogRepository
                .findByOrganizationIdAndActionOrderByCreatedAtDesc(
                        organizationId,
                        action
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // FILTER BY ACTOR
    // ============================================================

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getOrganizationAuditLogsByActor(
            Long organizationId,
            Long actorId) {

        return auditLogRepository
                .findByOrganizationIdAndActorIdOrderByCreatedAtDesc(
                        organizationId,
                        actorId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // FILTER BY ACTION + ACTOR
    // ============================================================

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getOrganizationAuditLogsByActionAndActor(
            Long organizationId,
            AuditAction action,
            Long actorId) {

        return auditLogRepository
                .findByOrganizationIdAndActionOrderByCreatedAtDesc(
                        organizationId,
                        action
                )
                .stream()
                .filter(log ->
                        log.getActor() != null
                                && log.getActor().getId().equals(actorId)
                )
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // ENTITY MAPPING
    // ============================================================

    private AuditLogResponse toResponse(
            AuditLog log) {

        return new AuditLogResponse(
                log.getId(),
                log.getOrganization().getId(),

                log.getActor() != null
                        ? log.getActor().getId()
                        : null,

                log.getActor() != null
                        ? log.getActor().getFullName()
                        : null,

                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}