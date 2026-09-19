package com.enterprise.assetmanagement.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // =========================================================
    // ADMIN
    // =========================================================

    // All projects in the organization
    List<Project> findByOrganizationId(Long organizationId);

    // =========================================================
    // PROJECT MANAGER
    // =========================================================

    // Only projects managed by this project manager
    List<Project> findByOrganizationIdAndManagerId(
            Long organizationId,
            Long managerId
    );

    // =========================================================
    // GENERAL PROJECT QUERIES
    // =========================================================

    List<Project> findByOrganizationIdAndStatus(
            Long organizationId,
            ProjectStatus status
    );

    // =========================================================
    // TEAM MEMBER
    // =========================================================

    /*
     * Return only projects that contain at least one task
     * assigned to the specified Team Member.
     *
     * DISTINCT prevents the same project from appearing
     * multiple times when the Team Member has multiple tasks
     * in that project.
     */
    @Query("""
        SELECT DISTINCT p
        FROM Project p
        JOIN Task t ON t.project.id = p.id
        WHERE p.organization.id = :organizationId
          AND t.assignee.id = :assigneeId
        ORDER BY p.id
        """)
    List<Project> findProjectsAssignedToTeamMember(
            @Param("organizationId") Long organizationId,
            @Param("assigneeId") Long assigneeId
    );

    // =========================================================
    // TENANT-SAFE LOOKUPS
    // =========================================================

    Optional<Project> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    // =========================================================
    // TENANT + MANAGER-SAFE LOOKUP
    // =========================================================

    Optional<Project> findByIdAndOrganizationIdAndManagerId(
            Long id,
            Long organizationId,
            Long managerId
    );

    // =========================================================
    // DUPLICATE PROJECT CHECK
    // =========================================================

    boolean existsByNameAndOrganizationId(
            String name,
            Long organizationId
    );
}