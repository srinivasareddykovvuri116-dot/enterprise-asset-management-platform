package com.enterprise.assetmanagement.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganizationId(Long organizationId);

    Optional<User> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<User> findByOrganizationIdAndRoleAndActiveTrue(
            Long organizationId,
            UserRole role
    );
}