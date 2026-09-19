package com.enterprise.assetmanagement.task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository
        extends JpaRepository<Task, Long>,
                JpaSpecificationExecutor<Task> {

    // ============================================================
    // FIND TASK BY ID + ORGANIZATION
    // ============================================================

    Optional<Task> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    // ============================================================
    // COUNT OVERDUE TASKS
    // ============================================================

    long countByOrganizationIdAndDueDateBeforeAndStatusNot(
            Long organizationId,
            LocalDate dueDate,
            TaskStatus status
    );

    // ============================================================
    // RESOURCE ALLOCATION
    // Team Member -> Number of Assigned Tasks
    // ============================================================

    @Query("""
        SELECT
            t.assignee.id,
            t.assignee.fullName,
            COUNT(t.id)
        FROM Task t
        WHERE t.organization.id = :organizationId
          AND t.assignee IS NOT NULL
          AND t.assignee.role = com.enterprise.assetmanagement.user.UserRole.TEAM_MEMBER
        GROUP BY
            t.assignee.id,
            t.assignee.fullName
        ORDER BY COUNT(t.id) DESC
        """)
    List<Object[]> findResourceAllocation(
            @Param("organizationId") Long organizationId
    );
}