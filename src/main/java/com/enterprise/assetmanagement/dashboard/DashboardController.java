package com.enterprise.assetmanagement.dashboard;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprise.assetmanagement.analytics.AnalyticsService;
import com.enterprise.assetmanagement.security.CustomUserDetails;
import com.enterprise.assetmanagement.user.UserRole;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final AnalyticsService analyticsService;

    public DashboardController(
            AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserRole role =
                UserRole.valueOf(
                        userDetails.getRole()
                );

        return ResponseEntity.ok(
                analyticsService.getDashboard(
                        userDetails.getOrganizationId(),
                        userDetails.getUserId(),
                        role
                )
        );
    }
}