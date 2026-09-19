package com.enterprise.assetmanagement.analytics;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enterprise.assetmanagement.task.Task;
import com.enterprise.assetmanagement.task.TaskPriority;
import com.enterprise.assetmanagement.task.TaskRepository;
import com.enterprise.assetmanagement.task.TaskStatus;
import com.enterprise.assetmanagement.user.UserRole;

@Service
public class AnalyticsService {

    private final TaskRepository taskRepository;

    public AnalyticsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ============================================================
    // ORGANIZATION SPECIFICATION
    // ============================================================

    private Specification<Task> organizationSpecification(
            Long organizationId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("organization").get("id"),
                        organizationId
                );
    }

    // ============================================================
    // PROJECT MANAGER SPECIFICATION
    // ============================================================

    private Specification<Task> projectManagerSpecification(
            Long organizationId,
            Long managerId) {

        return Specification
                .where(
                        organizationSpecification(
                                organizationId
                        )
                )
                .and(
                        (root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(
                                        root.get("project")
                                                .get("manager")
                                                .get("id"),
                                        managerId
                                )
                );
    }

    // ============================================================
    // TEAM MEMBER SPECIFICATION
    // ============================================================

    private Specification<Task> teamMemberSpecification(
            Long organizationId,
            Long userId) {

        return Specification
                .where(
                        organizationSpecification(
                                organizationId
                        )
                )
                .and(
                        (root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(
                                        root.get("assignee")
                                                .get("id"),
                                        userId
                                )
                );
    }

    // ============================================================
    // PUBLIC MANAGER TASK SPECIFICATION
    // ============================================================

    @Transactional(readOnly = true)
    public Specification<Task> getManagerTaskSpecification(
            Long organizationId,
            Long managerId) {

        return projectManagerSpecification(
                organizationId,
                managerId
        );
    }

    // ============================================================
    // TASKS BY STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public Map<TaskStatus, Long> getTasksByStatus(
            Specification<Task> specification) {

        List<Task> tasks =
                taskRepository.findAll(specification);

        Map<TaskStatus, Long> result =
                new EnumMap<>(TaskStatus.class);

        for (TaskStatus status : TaskStatus.values()) {
            result.put(status, 0L);
        }

        tasks.stream()
                .collect(
                        Collectors.groupingBy(
                                Task::getStatus,
                                Collectors.counting()
                        )
                )
                .forEach(result::put);

        return result;
    }

    // ============================================================
    // TASKS BY STATUS - ORGANIZATION COMPATIBILITY
    // ============================================================

    @Transactional(readOnly = true)
    public Map<TaskStatus, Long> getTasksByStatus(
            Long organizationId) {

        return getTasksByStatus(
                organizationSpecification(
                        organizationId
                )
        );
    }

    // ============================================================
    // TASKS BY PRIORITY
    // ============================================================

    @Transactional(readOnly = true)
    public Map<TaskPriority, Long> getTasksByPriority(
            Specification<Task> specification) {

        List<Task> tasks =
                taskRepository.findAll(specification);

        Map<TaskPriority, Long> result =
                new EnumMap<>(TaskPriority.class);

        for (TaskPriority priority : TaskPriority.values()) {
            result.put(priority, 0L);
        }

        tasks.stream()
                .collect(
                        Collectors.groupingBy(
                                Task::getPriority,
                                Collectors.counting()
                        )
                )
                .forEach(result::put);

        return result;
    }

    // ============================================================
    // TASKS BY PRIORITY - ORGANIZATION COMPATIBILITY
    // ============================================================

    @Transactional(readOnly = true)
    public Map<TaskPriority, Long> getTasksByPriority(
            Long organizationId) {

        return getTasksByPriority(
                organizationSpecification(
                        organizationId
                )
        );
    }

    // ============================================================
    // TOTAL TASKS
    // ============================================================

    @Transactional(readOnly = true)
    public long getTotalTasks(
            Specification<Task> specification) {

        return taskRepository.count(
                specification
        );
    }

    // ============================================================
    // TOTAL TASKS - ORGANIZATION COMPATIBILITY
    // ============================================================

    @Transactional(readOnly = true)
    public long getTotalTasks(
            Long organizationId) {

        return getTotalTasks(
                organizationSpecification(
                        organizationId
                )
        );
    }

    // ============================================================
    // COMPLETED TASKS
    // ============================================================

    @Transactional(readOnly = true)
    public long getCompletedTasks(
            Specification<Task> specification) {

        Specification<Task> completedSpecification =
                specification.and(
                        (root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(
                                        root.get("status"),
                                        TaskStatus.COMPLETED
                                )
                );

        return taskRepository.count(
                completedSpecification
        );
    }

    // ============================================================
    // COMPLETED TASKS - ORGANIZATION COMPATIBILITY
    // ============================================================

    @Transactional(readOnly = true)
    public long getCompletedTasks(
            Long organizationId) {

        return getCompletedTasks(
                organizationSpecification(
                        organizationId
                )
        );
    }

    // ============================================================
    // OVERDUE TASKS
    // ============================================================

    @Transactional(readOnly = true)
    public long getOverdueTasks(
            Specification<Task> specification) {

        Specification<Task> overdueSpecification =
                specification.and(
                        (root, query, criteriaBuilder) ->
                                criteriaBuilder.and(
                                        criteriaBuilder.lessThan(
                                                root.get("dueDate"),
                                                LocalDate.now()
                                        ),
                                        criteriaBuilder.notEqual(
                                                root.get("status"),
                                                TaskStatus.COMPLETED
                                        )
                                )
                );

        return taskRepository.count(
                overdueSpecification
        );
    }

    // ============================================================
    // OVERDUE TASKS - ORGANIZATION COMPATIBILITY
    // ============================================================

    @Transactional(readOnly = true)
    public long getOverdueTasks(
            Long organizationId) {

        return getOverdueTasks(
                organizationSpecification(
                        organizationId
                )
        );
    }

    // ============================================================
    // BUILD DASHBOARD
    // ============================================================

    private Map<String, Object> buildDashboard(
            Specification<Task> specification) {

        Map<String, Object> dashboard =
                new LinkedHashMap<>();

        long totalTasks =
                getTotalTasks(
                        specification
                );

        long completedTasks =
                getCompletedTasks(
                        specification
                );

        long pendingTasks =
                totalTasks - completedTasks;

        long overdueTasks =
                getOverdueTasks(
                        specification
                );

        dashboard.put(
                "totalTasks",
                totalTasks
        );

        dashboard.put(
                "completedTasks",
                completedTasks
        );

        dashboard.put(
                "pendingTasks",
                pendingTasks
        );

        dashboard.put(
                "overdueTasks",
                overdueTasks
        );

        dashboard.put(
                "tasksByStatus",
                getTasksByStatus(
                        specification
                )
        );

        dashboard.put(
                "tasksByPriority",
                getTasksByPriority(
                        specification
                )
        );

        return dashboard;
    }

    // ============================================================
    // ORGANIZATION DASHBOARD
    // ============================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(
            Long organizationId) {

        return buildDashboard(
                organizationSpecification(
                        organizationId
                )
        );
    }

    // ============================================================
    // ROLE-SCOPED DASHBOARD
    // ============================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(
            Long organizationId,
            Long userId,
            UserRole role) {

        if (organizationId == null) {
            throw new IllegalArgumentException(
                    "Organization ID is required"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (role == null) {
            throw new IllegalArgumentException(
                    "User role is required"
            );
        }

        Specification<Task> specification;

        switch (role) {

            case ORGANIZATION_ADMIN:

                specification =
                        organizationSpecification(
                                organizationId
                        );

                break;

            case PROJECT_MANAGER:

                specification =
                        projectManagerSpecification(
                                organizationId,
                                userId
                        );

                break;

            case TEAM_MEMBER:

                specification =
                        teamMemberSpecification(
                                organizationId,
                                userId
                        );

                break;

            default:

                throw new IllegalArgumentException(
                        "Unsupported user role: "
                                + role
                );
        }

        Map<String, Object> dashboard =
                buildDashboard(
                        specification
                );

        dashboard.put(
                "role",
                role.name()
        );

        return dashboard;
    }

    // ============================================================
    // RESOURCE ALLOCATION - ADMIN
    // ============================================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getResourceAllocation(
            Long organizationId) {

        return taskRepository
                .findResourceAllocation(
                        organizationId
                )
                .stream()
                .map(row -> {

                    Map<String, Object> result =
                            new LinkedHashMap<>();

                    result.put(
                            "userId",
                            row[0]
                    );

                    result.put(
                            "userName",
                            row[1]
                    );

                    result.put(
                            "assignedTasks",
                            row[2]
                    );

                    return result;
                })
                .toList();
    }

    // ============================================================
    // RESOURCE ALLOCATION - PROJECT MANAGER
    // ============================================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>>
    getResourceAllocationForManager(
            Long organizationId,
            Long managerId) {

        Specification<Task> specification =
                projectManagerSpecification(
                        organizationId,
                        managerId
                );

        List<Task> tasks =
                taskRepository.findAll(
                        specification
                );

        return tasks.stream()
                .filter(
                        task ->
                                task.getAssignee() != null
                )
                .filter(
                        task ->
                                task.getAssignee()
                                        .getRole()
                                        == UserRole.TEAM_MEMBER
                )
                .collect(
                        Collectors.groupingBy(
                                task ->
                                        task.getAssignee()
                                                .getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                )
                .entrySet()
                .stream()
                .map(entry -> {

                    Task firstTask =
                            entry.getValue()
                                    .get(0);

                    Map<String, Object> result =
                            new LinkedHashMap<>();

                    result.put(
                            "userId",
                            firstTask
                                    .getAssignee()
                                    .getId()
                    );

                    result.put(
                            "userName",
                            firstTask
                                    .getAssignee()
                                    .getFullName()
                    );

                    result.put(
                            "assignedTasks",
                            (long) entry
                                    .getValue()
                                    .size()
                    );

                    return result;
                })
                .toList();
    }

    // ============================================================
    // WORKSPACE DASHBOARD COMPATIBILITY
    // ============================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardForWorkspace(
            Long organizationId) {

        return buildDashboard(
                organizationSpecification(
                        organizationId
                )
        );
    }
}