package com.enterprise.assetmanagement.project;

import java.time.LocalDateTime;

import com.enterprise.assetmanagement.organization.Organization;
import com.enterprise.assetmanagement.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "projects",

    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_project_organization_name",
            columnNames = {
                "organization_id",
                "name"
            }
        )
    },

    indexes = {
        @Index(
            name = "idx_project_organization",
            columnList = "organization_id"
        ),
        @Index(
            name = "idx_project_status",
            columnList = "organization_id,status"
        ),
        @Index(
            name = "idx_project_manager",
            columnList = "manager_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        length = 150
    )
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "organization_id",
        nullable = false
    )
    private Organization organization;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "manager_id",
        nullable = false
    )
    private User manager;

    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}