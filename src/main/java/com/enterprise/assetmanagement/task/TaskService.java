package com.enterprise.assetmanagement.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.enterprise.assetmanagement.audit.AuditAction;
import com.enterprise.assetmanagement.audit.AuditLog;
import com.enterprise.assetmanagement.audit.AuditLogRepository;
import com.enterprise.assetmanagement.project.Project;
import com.enterprise.assetmanagement.project.ProjectRepository;
import com.enterprise.assetmanagement.project.ProjectStatus;
import com.enterprise.assetmanagement.security.CustomUserDetails;
import com.enterprise.assetmanagement.user.User;
import com.enterprise.assetmanagement.user.UserRepository;
import com.enterprise.assetmanagement.user.UserRole;

import jakarta.transaction.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ============================================================
    // CREATE TASK
    // ============================================================

    @Transactional
    public TaskResponse createTask(
            CreateTaskRequest request,
            Long organizationId,
            Long actorId) {

        User actor = getUserInOrganization(
                actorId,
                organizationId
        );

        // --------------------------------------------------------
        // ONLY ADMIN / PROJECT MANAGER CAN CREATE TASKS
        // --------------------------------------------------------

        if (actor.getRole() != UserRole.ORGANIZATION_ADMIN
                && actor.getRole() != UserRole.PROJECT_MANAGER) {

            throw new AccessDeniedException(
                    "Only organization administrators and project managers can create tasks"
            );
        }

        // --------------------------------------------------------
        // PROJECT
        // --------------------------------------------------------

        Project project = projectRepository
                .findByIdAndOrganizationId(
                        request.projectId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project not found"
                        )
                );

        // --------------------------------------------------------
        // PROJECT MANAGER OWNERSHIP
        // --------------------------------------------------------

        if (actor.getRole() == UserRole.PROJECT_MANAGER) {

            if (project.getManager() == null
                    || !project.getManager()
                    .getId()
                    .equals(actorId)) {

                throw new AccessDeniedException(
                        "Project managers can create tasks only in their assigned projects"
                );
            }
        }

        // --------------------------------------------------------
        // ARCHIVED PROJECT
        // --------------------------------------------------------

        if (project.getStatus() == ProjectStatus.ARCHIVED) {

            throw new IllegalArgumentException(
                    "Cannot create tasks in an archived project"
            );
        }

        // --------------------------------------------------------
        // ASSIGNEE
        // --------------------------------------------------------

        User assignee = null;

        if (request.assigneeId() != null) {

            assignee = getUserInOrganization(
                    request.assigneeId(),
                    organizationId
            );

            validateTeamMemberAssignee(assignee);

            if (actor.getRole() == UserRole.PROJECT_MANAGER) {

                if (project.getManager() == null
                        || !project.getManager()
                        .getId()
                        .equals(actorId)) {

                    throw new AccessDeniedException(
                            "Project managers can assign tasks only in their assigned projects"
                    );
                }
            }
        }

        // --------------------------------------------------------
        // CREATE TASK
        // --------------------------------------------------------

        Task task = new Task();

        task.setTitle(request.title());

        task.setDescription(
                request.description()
        );

        task.setStatus(
                TaskStatus.BACKLOG
        );

        task.setPriority(
                request.priority() != null
                        ? request.priority()
                        : TaskPriority.MEDIUM
        );

        task.setOrganization(
                project.getOrganization()
        );

        task.setProject(
                project
        );

        task.setAssignee(
                assignee
        );

        task.setDueDate(
                request.dueDate()
        );

        Task savedTask = taskRepository.save(task);

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(
                project.getOrganization()
        );

        auditLog.setActor(actor);

        auditLog.setAction(
                AuditAction.TASK_CREATED
        );

        auditLog.setEntityType("TASK");

        auditLog.setEntityId(
                savedTask.getId()
        );

        auditLog.setDetails(
                "Task created: "
                        + savedTask.getTitle()
        );

        auditLogRepository.save(auditLog);

        // IMPORTANT:
        // DTO conversion happens while the transaction/session is open.
        return toResponse(savedTask);
    }

    // ============================================================
    // GET TASKS
    // ============================================================

    @Transactional
    public Page<TaskResponse> getOrganizationTasks(
            Long organizationId,
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            Long assigneeId,
            Pageable pageable) {

        User actor = getAuthenticatedUser();

        if (!actor.getOrganization()
                .getId()
                .equals(organizationId)) {

            throw new AccessDeniedException(
                    "User does not belong to this organization"
            );
        }

        Specification<Task> specification =
                Specification.where(
                        TaskSpecification.belongsToOrganization(
                                organizationId
                        )
                );

        // --------------------------------------------------------
        // PROJECT MANAGER
        // --------------------------------------------------------

        if (actor.getRole() == UserRole.PROJECT_MANAGER) {

            specification = specification.and(
                    TaskSpecification.belongsToProjectManager(
                            actor.getId()
                    )
            );
        }

        // --------------------------------------------------------
        // TEAM MEMBER
        // --------------------------------------------------------

        if (actor.getRole() == UserRole.TEAM_MEMBER) {

            specification = specification.and(
                    TaskSpecification.assignedToUser(
                            actor.getId()
                    )
            );
        }

        // --------------------------------------------------------
        // STATUS FILTER
        // --------------------------------------------------------

        if (status != null) {

            specification = specification.and(
                    TaskSpecification.hasStatus(status)
            );
        }

        // --------------------------------------------------------
        // PRIORITY FILTER
        // --------------------------------------------------------

        if (priority != null) {

            specification = specification.and(
                    TaskSpecification.hasPriority(priority)
            );
        }

        // --------------------------------------------------------
        // PROJECT FILTER
        // --------------------------------------------------------

        if (projectId != null) {

            specification = specification.and(
                    TaskSpecification.belongsToProject(projectId)
            );
        }

        // --------------------------------------------------------
        // ASSIGNEE FILTER
        // --------------------------------------------------------

        if (assigneeId != null) {

            specification = specification.and(
                    TaskSpecification.assignedToUser(assigneeId)
            );
        }

        Page<Task> tasks = taskRepository.findAll(
                specification,
                pageable
        );

        /*
         * The Page is mapped while the @Transactional method is still
         * active. Therefore lazy Project and Assignee relationships
         * are available during DTO conversion.
         */
        return tasks.map(this::toResponse);
    }

    // ============================================================
    // GET SINGLE TASK
    // ============================================================

    @Transactional
    public TaskResponse getTaskResponse(
            Long taskId,
            Long organizationId) {

        Task task = getTask(
                taskId,
                organizationId
        );

        return toResponse(task);
    }

    // ============================================================
    // INTERNAL GET TASK
    // ============================================================

    @Transactional
    public Task getTask(
            Long taskId,
            Long organizationId) {

        User actor = getAuthenticatedUser();

        if (!actor.getOrganization()
                .getId()
                .equals(organizationId)) {

            throw new AccessDeniedException(
                    "User does not belong to this organization"
            );
        }

        Task task = taskRepository
                .findByIdAndOrganizationId(
                        taskId,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Task not found"
                        )
                );

        // --------------------------------------------------------
        // PROJECT MANAGER
        // --------------------------------------------------------

        if (actor.getRole() == UserRole.PROJECT_MANAGER) {

            if (task.getProject() == null
                    || task.getProject().getManager() == null
                    || !task.getProject()
                    .getManager()
                    .getId()
                    .equals(actor.getId())) {

                throw new AccessDeniedException(
                        "You do not have access to this task"
                );
            }
        }

        // --------------------------------------------------------
        // TEAM MEMBER
        // --------------------------------------------------------

        if (actor.getRole() == UserRole.TEAM_MEMBER) {

            if (task.getAssignee() == null
                    || !task.getAssignee()
                    .getId()
                    .equals(actor.getId())) {

                throw new AccessDeniedException(
                        "You do not have access to this task"
                );
            }
        }

        return task;
    }

    // ============================================================
    // UPDATE TASK
    // ============================================================
    //
    // Reachable only by ORGANIZATION_ADMIN and PROJECT_MANAGER
    // (enforced via @PreAuthorize on the controller endpoint).
    // Team members use updateTaskStatus() instead, so no
    // TEAM_MEMBER branching is needed here.
    // ============================================================

    @Transactional
    public TaskResponse updateTask(
            Long taskId,
            UpdateTaskRequest request,
            Long organizationId,
            Long actorId) {

        User actor = getUserInOrganization(
                actorId,
                organizationId
        );

        Task task = getTask(
                taskId,
                organizationId
        );

        // --------------------------------------------------------
        // TITLE
        // --------------------------------------------------------

        if (request.title() != null
                && !request.title().isBlank()) {

            task.setTitle(
                    request.title()
            );
        }

        // --------------------------------------------------------
        // DESCRIPTION
        // --------------------------------------------------------

        if (request.description() != null) {

            task.setDescription(
                    request.description()
            );
        }

        // --------------------------------------------------------
        // PRIORITY
        // --------------------------------------------------------

        if (request.priority() != null) {

            task.setPriority(
                    request.priority()
            );
        }

        // --------------------------------------------------------
        // STATUS
        // --------------------------------------------------------

        if (request.status() != null) {

            task.setStatus(
                    request.status()
            );
        }

        // --------------------------------------------------------
        // ASSIGNEE
        // --------------------------------------------------------

        if (request.assigneeId() != null) {

            User assignee =
                    getUserInOrganization(
                            request.assigneeId(),
                            organizationId
                    );

            validateTeamMemberAssignee(
                    assignee
            );

            if (actor.getRole()
                    == UserRole.PROJECT_MANAGER) {

                if (task.getProject() == null
                        || task.getProject()
                        .getManager() == null
                        || !task.getProject()
                        .getManager()
                        .getId()
                        .equals(actorId)) {

                    throw new AccessDeniedException(
                            "Project managers can assign tasks only in their assigned projects"
                    );
                }
            }

            task.setAssignee(
                    assignee
            );
        }

        // --------------------------------------------------------
        // DUE DATE
        // --------------------------------------------------------

        if (request.dueDate() != null) {

            task.setDueDate(
                    request.dueDate()
            );
        }

        // --------------------------------------------------------
        // SAVE
        // --------------------------------------------------------

        Task updatedTask =
                taskRepository.save(task);

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(
                task.getOrganization()
        );

        auditLog.setActor(actor);

        auditLog.setEntityType("TASK");

        auditLog.setEntityId(
                task.getId()
        );

        if (request.status() != null) {

            auditLog.setAction(
                    AuditAction.TASK_STATUS_CHANGED
            );

            auditLog.setDetails(
                    "Task status changed to: "
                            + request.status().name()
            );

        } else if (request.assigneeId() != null) {

            auditLog.setAction(
                    AuditAction.TASK_ASSIGNED
            );

            auditLog.setDetails(
                    "Task assigned to user: "
                            + request.assigneeId()
            );

        } else {

            auditLog.setAction(
                    AuditAction.TASK_UPDATED
            );

            auditLog.setDetails(
                    "Task updated: "
                            + task.getTitle()
            );
        }

        auditLogRepository.save(auditLog);

        return toResponse(updatedTask);
    }

    // ============================================================
    // UPDATE TASK STATUS
    // ============================================================

    @Transactional
    public TaskResponse updateTaskStatus(
            Long taskId,
            UpdateTaskStatusRequest request,
            Long organizationId,
            Long actorId) {

        User actor = getUserInOrganization(
                actorId,
                organizationId
        );

        Task task = getTask(
                taskId,
                organizationId
        );

        TaskStatus oldStatus =
                task.getStatus();

        task.setStatus(
                request.status()
        );

        Task updatedTask =
                taskRepository.save(task);

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(
                task.getOrganization()
        );

        auditLog.setActor(actor);

        auditLog.setAction(
                AuditAction.TASK_STATUS_CHANGED
        );

        auditLog.setEntityType("TASK");

        auditLog.setEntityId(
                task.getId()
        );

        auditLog.setDetails(
                "Task status changed from "
                        + oldStatus.name()
                        + " to "
                        + request.status().name()
        );

        auditLogRepository.save(auditLog);

        return toResponse(updatedTask);
    }

    // ============================================================
    // ASSIGN TASK
    // ============================================================

    @Transactional
    public TaskResponse assignTask(
            Long taskId,
            AssignTaskRequest request,
            Long organizationId,
            Long actorId) {

        User actor = getUserInOrganization(
                actorId,
                organizationId
        );

        // --------------------------------------------------------
        // ONLY ADMIN / PROJECT MANAGER
        // --------------------------------------------------------

        if (actor.getRole() != UserRole.ORGANIZATION_ADMIN
                && actor.getRole() != UserRole.PROJECT_MANAGER) {

            throw new AccessDeniedException(
                    "Only organization administrators and project managers can assign tasks"
            );
        }

        Task task = getTask(
                taskId,
                organizationId
        );

        // --------------------------------------------------------
        // PM OWNERSHIP
        // --------------------------------------------------------

        if (actor.getRole()
                == UserRole.PROJECT_MANAGER) {

            if (task.getProject() == null
                    || task.getProject()
                    .getManager() == null
                    || !task.getProject()
                    .getManager()
                    .getId()
                    .equals(actorId)) {

                throw new AccessDeniedException(
                        "Project managers can assign tasks only in their assigned projects"
                );
            }
        }

        // --------------------------------------------------------
        // ASSIGNEE
        // --------------------------------------------------------

        User assignee =
                getUserInOrganization(
                        request.assigneeId(),
                        organizationId
                );

        validateTeamMemberAssignee(
                assignee
        );

        task.setAssignee(
                assignee
        );

        Task updatedTask =
                taskRepository.save(task);

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(
                task.getOrganization()
        );

        auditLog.setActor(actor);

        auditLog.setAction(
                AuditAction.TASK_ASSIGNED
        );

        auditLog.setEntityType("TASK");

        auditLog.setEntityId(
                task.getId()
        );

        auditLog.setDetails(
                "Task assigned to user: "
                        + assignee.getEmail()
        );

        auditLogRepository.save(auditLog);

        return toResponse(updatedTask);
    }

    // ============================================================
    // VALIDATE ASSIGNEE
    // ============================================================

    private void validateTeamMemberAssignee(
            User assignee) {

        if (assignee.getRole()
                != UserRole.TEAM_MEMBER) {

            throw new IllegalArgumentException(
                    "Tasks can only be assigned to team members"
            );
        }

        if (!assignee.isActive()) {

            throw new IllegalArgumentException(
                    "Cannot assign a task to an inactive team member"
            );
        }
    }

    // ============================================================
    // ENTITY → DTO
    // ============================================================

    private TaskResponse toResponse(Task task) {

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),

                task.getOrganization().getId(),

                task.getProject().getId(),
                task.getProject().getName(),

                task.getAssignee() != null
                        ? task.getAssignee().getId()
                        : null,

                task.getAssignee() != null
                        ? task.getAssignee().getFullName()
                        : null
        );
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private User getUserInOrganization(
            Long userId,
            Long organizationId) {

        return userRepository
                .findByIdAndOrganizationId(
                        userId,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found in organization"
                        )
                );
    }

    private User getAuthenticatedUser() {

        CustomUserDetails userDetails =
                (CustomUserDetails)
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getPrincipal();

        return userRepository
                .findById(
                        userDetails.getUserId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }
}
