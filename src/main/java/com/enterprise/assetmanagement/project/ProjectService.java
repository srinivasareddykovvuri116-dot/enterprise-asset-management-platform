package com.enterprise.assetmanagement.project;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enterprise.assetmanagement.audit.AuditAction;
import com.enterprise.assetmanagement.audit.AuditLog;
import com.enterprise.assetmanagement.audit.AuditLogRepository;
import com.enterprise.assetmanagement.organization.Organization;
import com.enterprise.assetmanagement.organization.OrganizationRepository;
import com.enterprise.assetmanagement.security.CustomUserDetails;
import com.enterprise.assetmanagement.user.User;
import com.enterprise.assetmanagement.user.UserRepository;
import com.enterprise.assetmanagement.user.UserRole;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {

        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // =========================================================
    // CREATE PROJECT
    // =========================================================

    @Transactional
    public ProjectResponse createProject(
            CreateProjectRequest request,
            Long organizationId,
            Long actorId) {

        User actor = getUserInOrganization(actorId, organizationId);

        if (actor.getRole() != UserRole.ORGANIZATION_ADMIN
                && actor.getRole() != UserRole.PROJECT_MANAGER) {

            throw new AccessDeniedException(
                    "Only organization administrators and project managers can create projects"
            );
        }

        if (projectRepository.existsByNameAndOrganizationId(
                request.name(),
                organizationId)) {

            throw new IllegalArgumentException(
                    "Project name already exists in this organization"
            );
        }

        Organization organization =
                organizationRepository.findById(organizationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found"
                                )
                        );

        User manager = getUserInOrganization(
                request.managerId(),
                organizationId
        );

        if (manager.getRole() != UserRole.PROJECT_MANAGER
                && manager.getRole() != UserRole.ORGANIZATION_ADMIN) {

            throw new IllegalArgumentException(
                    "Project manager must have PROJECT_MANAGER or ORGANIZATION_ADMIN role"
            );
        }

        /*
         * Project Manager can create a project only for themselves.
         * Admin can create a project for any valid manager.
         */
        if (actor.getRole() == UserRole.PROJECT_MANAGER
                && !manager.getId().equals(actor.getId())) {

            throw new AccessDeniedException(
                    "Project managers can create projects only for themselves"
            );
        }

        Project project = new Project();

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOrganization(organization);
        project.setManager(manager);

        Project savedProject = projectRepository.save(project);

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(organization);
        auditLog.setActor(actor);
        auditLog.setAction(AuditAction.PROJECT_CREATED);
        auditLog.setEntityType("PROJECT");
        auditLog.setEntityId(savedProject.getId());
        auditLog.setDetails(
                "Created project: " + savedProject.getName()
        );

        auditLogRepository.save(auditLog);

        return toResponse(savedProject);
    }

    // =========================================================
    // GET PROJECTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(Long organizationId) {

        User actor = getAuthenticatedUser();

        if (!actor.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException(
                    "User does not belong to this organization"
            );
        }

        /*
         * ADMIN:
         * See every project in the organization.
         */
        if (actor.getRole() == UserRole.ORGANIZATION_ADMIN) {

            return projectRepository.findByOrganizationId(organizationId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        /*
         * PROJECT MANAGER:
         * See only projects assigned to this manager.
         */
        if (actor.getRole() == UserRole.PROJECT_MANAGER) {

            return projectRepository
                    .findByOrganizationIdAndManagerId(
                            organizationId,
                            actor.getId()
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        /*
         * TEAM MEMBER:
         *
         * See only projects that contain at least one
         * task assigned to this Team Member.
         */
        if (actor.getRole() == UserRole.TEAM_MEMBER) {

            return projectRepository
                    .findProjectsAssignedToTeamMember(
                            organizationId,
                            actor.getId()
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        throw new AccessDeniedException(
                "Unsupported user role"
        );
    }

    // =========================================================
    // GET SINGLE PROJECT
    // =========================================================

    @Transactional(readOnly = true)
    public ProjectResponse getProjectResponse(
            Long projectId,
            Long organizationId) {

        return toResponse(
                getProject(projectId, organizationId)
        );
    }

    /*
     * Internal entity-based method.
     *
     * This remains useful when another service needs the
     * actual Project entity.
     */
    @Transactional(readOnly = true)
    public Project getProject(
            Long projectId,
            Long organizationId) {

        User actor = getAuthenticatedUser();

        if (!actor.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException(
                    "User does not belong to this organization"
            );
        }

        /*
         * ADMIN:
         * Can access any project in the organization.
         */
        if (actor.getRole() == UserRole.ORGANIZATION_ADMIN) {

            return projectRepository
                    .findByIdAndOrganizationId(
                            projectId,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Project not found"
                            )
                    );
        }

        /*
         * PROJECT MANAGER:
         * Can access only projects assigned to them.
         */
        if (actor.getRole() == UserRole.PROJECT_MANAGER) {

            return projectRepository
                    .findByIdAndOrganizationIdAndManagerId(
                            projectId,
                            organizationId,
                            actor.getId()
                    )
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "You do not have access to this project"
                            )
                    );
        }

        /*
         * TEAM MEMBER:
         *
         * A Team Member can access a project only when
         * the project contains a task assigned to them.
         */
        if (actor.getRole() == UserRole.TEAM_MEMBER) {

            return projectRepository
                    .findProjectsAssignedToTeamMember(
                            organizationId,
                            actor.getId()
                    )
                    .stream()
                    .filter(project ->
                            project.getId().equals(projectId)
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "You do not have access to this project"
                            )
                    );
        }

        throw new AccessDeniedException(
                "Unsupported user role"
        );
    }

    // =========================================================
    // UPDATE PROJECT
    // =========================================================

    @Transactional
    public ProjectResponse updateProject(
            Long projectId,
            UpdateProjectRequest request) {

        User actor = getAuthenticatedUser();

        Long organizationId =
                actor.getOrganization().getId();

        if (actor.getRole() != UserRole.ORGANIZATION_ADMIN
                && actor.getRole() != UserRole.PROJECT_MANAGER) {

            throw new AccessDeniedException(
                    "Only organization administrators and project managers can update projects"
            );
        }

        Project project;

        /*
         * ADMIN:
         * Can update any organization project.
         */
        if (actor.getRole() == UserRole.ORGANIZATION_ADMIN) {

            project = projectRepository
                    .findByIdAndOrganizationId(
                            projectId,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Project not found"
                            )
                    );
        }

        /*
         * PROJECT MANAGER:
         * Can update only their assigned projects.
         */
        else {

            project = projectRepository
                    .findByIdAndOrganizationIdAndManagerId(
                            projectId,
                            organizationId,
                            actor.getId()
                    )
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "You do not have access to this project"
                            )
                    );
        }

        if (request.name() != null
                && !request.name().isBlank()
                && !request.name().equals(project.getName())) {

            if (projectRepository.existsByNameAndOrganizationId(
                    request.name(),
                    organizationId)) {

                throw new IllegalArgumentException(
                        "Project name already exists"
                );
            }

            project.setName(request.name());
        }

        if (request.description() != null) {
            project.setDescription(request.description());
        }

        if (request.status() != null) {
            project.setStatus(request.status());
        }

        /*
         * Only Admin should reassign a project to another manager.
         */
        if (request.managerId() != null) {

            if (actor.getRole() != UserRole.ORGANIZATION_ADMIN) {

                throw new AccessDeniedException(
                        "Only organization administrators can change the project manager"
                );
            }

            User manager = userRepository
                    .findByIdAndOrganizationId(
                            request.managerId(),
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Project manager not found"
                            )
                    );

            if (manager.getRole() != UserRole.PROJECT_MANAGER
                    && manager.getRole() != UserRole.ORGANIZATION_ADMIN) {

                throw new IllegalArgumentException(
                        "Selected user cannot manage projects"
                );
            }

            project.setManager(manager);
        }

        Project updatedProject =
                projectRepository.save(project);

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(actor.getOrganization());
        auditLog.setActor(actor);
        auditLog.setAction(AuditAction.PROJECT_UPDATED);
        auditLog.setEntityType("PROJECT");
        auditLog.setEntityId(project.getId());
        auditLog.setDetails("Project updated");

        auditLogRepository.save(auditLog);

        return toResponse(updatedProject);
    }

    // =========================================================
    // ARCHIVE PROJECT
    // =========================================================

    @Transactional
    public ProjectResponse archiveProject(Long projectId) {

        User actor = getAuthenticatedUser();

        if (actor.getRole() != UserRole.ORGANIZATION_ADMIN
                && actor.getRole() != UserRole.PROJECT_MANAGER) {

            throw new AccessDeniedException(
                    "Only organization administrators and project managers can archive projects"
            );
        }

        Long organizationId =
                actor.getOrganization().getId();

        Project project;

        if (actor.getRole() == UserRole.ORGANIZATION_ADMIN) {

            project = projectRepository
                    .findByIdAndOrganizationId(
                            projectId,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Project not found"
                            )
                    );

        } else {

            project = projectRepository
                    .findByIdAndOrganizationIdAndManagerId(
                            projectId,
                            organizationId,
                            actor.getId()
                    )
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "You do not have access to this project"
                            )
                    );
        }

        if (project.getStatus() == ProjectStatus.ARCHIVED) {

            throw new IllegalArgumentException(
                    "Project is already archived"
            );
        }

        project.setStatus(ProjectStatus.ARCHIVED);

        Project archivedProject =
                projectRepository.save(project);

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(actor.getOrganization());
        auditLog.setActor(actor);
        auditLog.setAction(AuditAction.PROJECT_ARCHIVED);
        auditLog.setEntityType("PROJECT");
        auditLog.setEntityId(project.getId());
        auditLog.setDetails(
                "Archived project: " + project.getName()
        );

        auditLogRepository.save(auditLog);

        return toResponse(archivedProject);
    }

    // =========================================================
    // RESTORE PROJECT
    // =========================================================

    @Transactional
    public ProjectResponse restoreProject(Long projectId) {

        User actor = getAuthenticatedUser();

        if (actor.getRole() != UserRole.ORGANIZATION_ADMIN
                && actor.getRole() != UserRole.PROJECT_MANAGER) {

            throw new AccessDeniedException(
                    "Only organization administrators and project managers can restore projects"
            );
        }

        Long organizationId =
                actor.getOrganization().getId();

        Project project;

        if (actor.getRole() == UserRole.ORGANIZATION_ADMIN) {

            project = projectRepository
                    .findByIdAndOrganizationId(
                            projectId,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Project not found"
                            )
                    );

        } else {

            project = projectRepository
                    .findByIdAndOrganizationIdAndManagerId(
                            projectId,
                            organizationId,
                            actor.getId()
                    )
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "You do not have access to this project"
                            )
                    );
        }

        if (project.getStatus() != ProjectStatus.ARCHIVED) {

            throw new IllegalArgumentException(
                    "Only archived projects can be restored"
            );
        }

        project.setStatus(ProjectStatus.ACTIVE);

        Project restoredProject =
                projectRepository.save(project);

        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(actor.getOrganization());
        auditLog.setActor(actor);
        auditLog.setAction(AuditAction.PROJECT_UPDATED);
        auditLog.setEntityType("PROJECT");
        auditLog.setEntityId(project.getId());
        auditLog.setDetails(
                "Restored project: " + project.getName()
        );

        auditLogRepository.save(auditLog);

        return toResponse(restoredProject);
    }

    // =========================================================
    // ENTITY -> RESPONSE MAPPING
    // =========================================================

    private ProjectResponse toResponse(Project project) {

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getOrganization().getId(),
                project.getManager() != null
                        ? project.getManager().getId()
                        : null,
                project.getManager() != null
                        ? project.getManager().getFullName()
                        : null
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private User getUserInOrganization(
            Long userId,
            Long organizationId) {

        return userRepository
                .findByIdAndOrganizationId(
                        userId,
                        organizationId
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "User does not belong to this organization"
                        )
                );
    }

    private User getAuthenticatedUser() {

        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        return userRepository
                .findById(userDetails.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }
}