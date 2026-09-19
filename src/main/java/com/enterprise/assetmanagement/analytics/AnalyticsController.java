package com.enterprise.assetmanagement.analytics;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprise.assetmanagement.security.CustomUserDetails;
import com.enterprise.assetmanagement.task.TaskPriority;
import com.enterprise.assetmanagement.task.TaskStatus;
import com.enterprise.assetmanagement.user.UserRole;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(
            AnalyticsService analyticsService) {

        this.analyticsService = analyticsService;
    }

    // ============================================================
    // HELPER
    // ============================================================

    private UserRole getUserRole(
            CustomUserDetails userDetails) {

        return UserRole.valueOf(
                userDetails.getRole()
        );
    }

    // ============================================================
    // TASKS BY STATUS
    // ============================================================

    @GetMapping("/tasks/status")
    public ResponseEntity<Map<TaskStatus, Long>> getTasksByStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                getUserRole(userDetails);

        if (role == UserRole.PROJECT_MANAGER) {

            return ResponseEntity.ok(
                    analyticsService.getTasksByStatus(
                            analyticsService
                                    .getManagerTaskSpecification(
                                            userDetails.getOrganizationId(),
                                            userDetails.getUserId()
                                    )
                    )
            );
        }

        return ResponseEntity.ok(
                analyticsService.getTasksByStatus(
                        userDetails.getOrganizationId()
                )
        );
    }

    // ============================================================
    // TASKS BY PRIORITY
    // ============================================================

    @GetMapping("/tasks/priority")
    public ResponseEntity<Map<TaskPriority, Long>> getTasksByPriority(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                getUserRole(userDetails);

        if (role == UserRole.PROJECT_MANAGER) {

            return ResponseEntity.ok(
                    analyticsService.getTasksByPriority(
                            analyticsService
                                    .getManagerTaskSpecification(
                                            userDetails.getOrganizationId(),
                                            userDetails.getUserId()
                                    )
                    )
            );
        }

        return ResponseEntity.ok(
                analyticsService.getTasksByPriority(
                        userDetails.getOrganizationId()
                )
        );
    }

    // ============================================================
    // TOTAL TASKS
    // ============================================================

    @GetMapping("/tasks/total")
    public ResponseEntity<Map<String, Long>> getTotalTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                getUserRole(userDetails);

        long totalTasks;

        if (role == UserRole.PROJECT_MANAGER) {

            totalTasks =
                    analyticsService.getTotalTasks(
                            analyticsService
                                    .getManagerTaskSpecification(
                                            userDetails.getOrganizationId(),
                                            userDetails.getUserId()
                                    )
                    );

        } else {

            totalTasks =
                    analyticsService.getTotalTasks(
                            userDetails.getOrganizationId()
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "totalTasks",
                        totalTasks
                )
        );
    }

    // ============================================================
    // COMPLETED TASKS
    // ============================================================

    @GetMapping("/tasks/completed")
    public ResponseEntity<Map<String, Long>> getCompletedTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                getUserRole(userDetails);

        long completedTasks;

        if (role == UserRole.PROJECT_MANAGER) {

            completedTasks =
                    analyticsService.getCompletedTasks(
                            analyticsService
                                    .getManagerTaskSpecification(
                                            userDetails.getOrganizationId(),
                                            userDetails.getUserId()
                                    )
                    );

        } else {

            completedTasks =
                    analyticsService.getCompletedTasks(
                            userDetails.getOrganizationId()
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "completedTasks",
                        completedTasks
                )
        );
    }

    // ============================================================
    // OVERDUE TASKS
    // ============================================================

    @GetMapping("/tasks/overdue")
    public ResponseEntity<Map<String, Long>> getOverdueTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                getUserRole(userDetails);

        long overdueTasks;

        if (role == UserRole.PROJECT_MANAGER) {

            overdueTasks =
                    analyticsService.getOverdueTasks(
                            analyticsService
                                    .getManagerTaskSpecification(
                                            userDetails.getOrganizationId(),
                                            userDetails.getUserId()
                                    )
                    );

        } else {

            overdueTasks =
                    analyticsService.getOverdueTasks(
                            userDetails.getOrganizationId()
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "overdueTasks",
                        overdueTasks
                )
        );
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                getUserRole(userDetails);

        return ResponseEntity.ok(
                analyticsService.getDashboard(
                        userDetails.getOrganizationId(),
                        userDetails.getUserId(),
                        role
                )
        );
    }

    // ============================================================
    // RESOURCE ALLOCATION
    // ============================================================

    @GetMapping("/tasks/resource-allocation")
    public ResponseEntity<List<Map<String, Object>>>
    getResourceAllocation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                getUserRole(userDetails);

        if (role == UserRole.PROJECT_MANAGER) {

            return ResponseEntity.ok(
                    analyticsService
                            .getResourceAllocationForManager(
                                    userDetails.getOrganizationId(),
                                    userDetails.getUserId()
                            )
            );
        }

        return ResponseEntity.ok(
                analyticsService.getResourceAllocation(
                        userDetails.getOrganizationId()
                )
        );
    }
}