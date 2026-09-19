package com.enterprise.assetmanagement.task;

import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    // ============================================================
    // ORGANIZATION
    // ============================================================

    public static Specification<Task> belongsToOrganization(
            Long organizationId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("organization").get("id"),
                        organizationId
                );
    }

    // ============================================================
    // PROJECT MANAGER
    // ============================================================

    public static Specification<Task> belongsToProjectManager(
            Long managerId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("project")
                                .get("manager")
                                .get("id"),
                        managerId
                );
    }

    // ============================================================
    // STATUS
    // ============================================================

    public static Specification<Task> hasStatus(
            TaskStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    // ============================================================
    // PRIORITY
    // ============================================================

    public static Specification<Task> hasPriority(
            TaskPriority priority) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("priority"),
                        priority
                );
    }

    // ============================================================
    // PROJECT
    // ============================================================

    public static Specification<Task> belongsToProject(
            Long projectId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("project").get("id"),
                        projectId
                );
    }

    // ============================================================
    // ASSIGNEE
    // ============================================================

    public static Specification<Task> assignedToUser(
            Long assigneeId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("assignee").get("id"),
                        assigneeId
                );
    }
}