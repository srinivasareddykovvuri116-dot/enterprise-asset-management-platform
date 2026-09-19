package com.enterprise.assetmanagement.user;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enterprise.assetmanagement.audit.AuditAction;
import com.enterprise.assetmanagement.audit.AuditLog;
import com.enterprise.assetmanagement.audit.AuditLogRepository;
import com.enterprise.assetmanagement.organization.Organization;
import com.enterprise.assetmanagement.organization.OrganizationRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    public UserService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder,
            AuditLogRepository auditLogRepository) {

        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogRepository = auditLogRepository;
    }

    // ============================================================
    // CREATE USER
    // ============================================================

    @Transactional
    public UserResponse createUser(
            CreateUserRequest request,
            Long organizationId,
            Long actorId) {

        // Check duplicate email
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        // Verify actor belongs to this organization
        User actor = userRepository
                .findByIdAndOrganizationId(
                        actorId,
                        organizationId
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Actor does not belong to this organization"
                        )
                );

        // Only organization admin can create users
        if (actor.getRole() != UserRole.ORGANIZATION_ADMIN) {
            throw new AccessDeniedException(
                    "Only organization administrators can create users"
            );
        }

        // Find organization
        Organization organization =
                organizationRepository.findById(organizationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found"
                                )
                        );

        // Create user
        User user = new User();

        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setOrganization(organization);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Create audit log
        AuditLog auditLog = new AuditLog();

        auditLog.setOrganization(organization);
        auditLog.setActor(actor);
        auditLog.setAction(AuditAction.USER_CREATED);
        auditLog.setEntityType("USER");
        auditLog.setEntityId(savedUser.getId());
        auditLog.setDetails(
                "Created user with email: " + savedUser.getEmail()
        );

        auditLogRepository.save(auditLog);

        // Return DTO
        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getOrganization().getId(),
                savedUser.isActive()
        );
    }

    // ============================================================
    // GET ALL USERS
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(Long organizationId) {

        return userRepository
                .findByOrganizationId(organizationId)
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole(),
                        user.getOrganization().getId(),
                        user.isActive()
                ))
                .toList();
    }

    // ============================================================
    // GET ASSIGNABLE USERS
    //
    // Used by:
    // - Organization Admin
    // - Project Manager
    //
    // Returns only:
    // - same organization
    // - active users
    // - TEAM_MEMBER role
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAssignableUsers(
            Long organizationId) {

        return userRepository
                .findByOrganizationIdAndRoleAndActiveTrue(
                        organizationId,
                        UserRole.TEAM_MEMBER
                )
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole(),
                        user.getOrganization().getId(),
                        user.isActive()
                ))
                .toList();
    }
}