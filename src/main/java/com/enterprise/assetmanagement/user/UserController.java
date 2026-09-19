package com.enterprise.assetmanagement.user;

import com.enterprise.assetmanagement.security.CustomUserDetails;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ============================================================
    // CREATE USER
    // ADMIN ONLY
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        UserResponse response = userService.createUser(
                request,
                currentUser.getOrganizationId(),
                currentUser.getUserId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ============================================================
    // GET ALL USERS
    // ADMIN ONLY
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        List<UserResponse> users =
                userService.getUsers(
                        currentUser.getOrganizationId()
                );

        return ResponseEntity.ok(users);
    }

    // ============================================================
    // GET ASSIGNABLE USERS
    //
    // ADMIN + PROJECT MANAGER
    //
    // Returns:
    // - same organization
    // - active users
    // - TEAM_MEMBER only
    // ============================================================

    @GetMapping("/assignable")
    @PreAuthorize(
            "hasAnyRole('ORGANIZATION_ADMIN', 'PROJECT_MANAGER')"
    )
    public ResponseEntity<List<UserResponse>> getAssignableUsers(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        List<UserResponse> users =
                userService.getAssignableUsers(
                        currentUser.getOrganizationId()
                );

        return ResponseEntity.ok(users);
    }
}