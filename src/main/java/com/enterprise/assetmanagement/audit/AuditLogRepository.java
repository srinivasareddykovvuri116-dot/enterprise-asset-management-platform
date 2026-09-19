package com.enterprise.assetmanagement.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );

    List<AuditLog> findByOrganizationIdAndActionOrderByCreatedAtDesc(
            Long organizationId,
            AuditAction action
    );

    List<AuditLog> findByOrganizationIdAndActorIdOrderByCreatedAtDesc(
            Long organizationId,
            Long actorId
    );
}