package com.enterprise.assetmanagement.audit;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(
            name = "idx_audit_organization",
            columnList = "organization_id"
        ),
        @Index(
            name = "idx_audit_actor",
            columnList = "actor_id"
        ),
        @Index(
            name = "idx_audit_created_at",
            columnList = "organization_id,created_at"
        ),
        @Index(
            name = "idx_audit_action",
            columnList = "organization_id,action"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    @Column(length = 100)
    private String entityType;

    @Column
    private Long entityId;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}