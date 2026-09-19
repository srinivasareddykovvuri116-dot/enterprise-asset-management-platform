package com.enterprise.assetmanagement.audit;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enterprise.assetmanagement.security.CustomUserDetails;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(
            AuditLogService auditLogService) {

        this.auditLogService = auditLogService;
    }

    // ============================================================
    // GET AUDIT LOGS
    //
    // Supported:
    //
    // GET /api/audit-logs
    // GET /api/audit-logs?action=TASK_CREATED
    // GET /api/audit-logs?actorId=1732
    // GET /api/audit-logs?action=TASK_CREATED&actorId=1732
    //
    // ============================================================

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @RequestParam(required = false)
            AuditAction action,

            @RequestParam(required = false)
            Long actorId) {

        Long organizationId =
                userDetails.getOrganizationId();

        List<AuditLogResponse> logs;

        // ========================================================
        // ACTION + ACTOR
        // ========================================================

        if (action != null && actorId != null) {

            logs =
                    auditLogService
                            .getOrganizationAuditLogsByActionAndActor(
                                    organizationId,
                                    action,
                                    actorId
                            );

        }

        // ========================================================
        // ACTION ONLY
        // ========================================================

        else if (action != null) {

            logs =
                    auditLogService
                            .getOrganizationAuditLogsByAction(
                                    organizationId,
                                    action
                            );

        }

        // ========================================================
        // ACTOR ONLY
        // ========================================================

        else if (actorId != null) {

            logs =
                    auditLogService
                            .getOrganizationAuditLogsByActor(
                                    organizationId,
                                    actorId
                            );

        }

        // ========================================================
        // NO FILTER
        // ========================================================

        else {

            logs =
                    auditLogService
                            .getOrganizationAuditLogs(
                                    organizationId
                            );
        }

        return ResponseEntity.ok(logs);
    }
}