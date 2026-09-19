package com.enterprise.assetmanagement.project;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprise.assetmanagement.security.CustomUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(
                projectService.createProject(
                        request,
                        userDetails.getOrganizationId(),
                        userDetails.getUserId()
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(
                projectService.getProjects(
                        userDetails.getOrganizationId()
                )
        );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long projectId,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(
                projectService.getProjectResponse(
                        projectId,
                        userDetails.getOrganizationId()
                )
        );
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        return ResponseEntity.ok(
                projectService.updateProject(
                        projectId,
                        request
                )
        );
    }

    @PatchMapping("/{projectId}/archive")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                projectService.archiveProject(projectId)
        );
    }

    @PatchMapping("/{projectId}/restore")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponse> restoreProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                projectService.restoreProject(projectId)
        );
    }
}