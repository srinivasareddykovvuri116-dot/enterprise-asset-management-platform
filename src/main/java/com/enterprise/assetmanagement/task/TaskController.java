package com.enterprise.assetmanagement.task;

import com.enterprise.assetmanagement.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ============================================================
    // CREATE TASK
    // ============================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TaskResponse response = taskService.createTask(
                request,
                userDetails.getOrganizationId(),
                userDetails.getUserId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ============================================================
    // GET TASKS WITH FILTERING + PAGINATION
    // ============================================================

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt"
            ) Pageable pageable) {

        Page<TaskResponse> tasks = taskService.getOrganizationTasks(
                userDetails.getOrganizationId(),
                status,
                priority,
                projectId,
                assigneeId,
                pageable
        );

        return ResponseEntity.ok(tasks);
    }

    // ============================================================
    // GET SINGLE TASK
    // ============================================================

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TaskResponse response = taskService.getTaskResponse(
                taskId,
                userDetails.getOrganizationId()
        );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE TASK
    // ============================================================
    //
    // Full task edits (title, description, priority, due date,
    // assignee) are restricted to organization admins and project
    // managers. Team members must use the narrower
    // PATCH /{taskId}/status endpoint below.
    // ============================================================

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TaskResponse response = taskService.updateTask(
                taskId,
                request,
                userDetails.getOrganizationId(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE TASK STATUS
    // ============================================================

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TaskResponse response = taskService.updateTaskStatus(
                taskId,
                request,
                userDetails.getOrganizationId(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ASSIGN TASK
    // ============================================================

    @PatchMapping("/{taskId}/assignee")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TaskResponse response = taskService.assignTask(
                taskId,
                request,
                userDetails.getOrganizationId(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(response);
    }
}
