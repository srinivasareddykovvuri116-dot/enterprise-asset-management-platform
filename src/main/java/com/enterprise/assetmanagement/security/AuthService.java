package com.enterprise.assetmanagement.security;

import com.enterprise.assetmanagement.organization.Organization;
import com.enterprise.assetmanagement.organization.OrganizationRepository;
import com.enterprise.assetmanagement.user.User;
import com.enterprise.assetmanagement.user.UserRepository;
import com.enterprise.assetmanagement.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        if (organizationRepository.existsByName(
                request.organizationName())) {

            throw new IllegalArgumentException(
                    "Organization name is already registered"
            );
        }

        Organization organization = new Organization();
        organization.setName(request.organizationName());

        Organization savedOrganization =
                organizationRepository.save(organization);

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setFullName(request.fullName());
        user.setRole(UserRole.ORGANIZATION_ADMIN);
        user.setOrganization(savedOrganization);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails =
                new CustomUserDetails(savedUser);

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedOrganization.getId(),
                savedUser.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        CustomUserDetails userDetails =
                new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getId(),
                user.getOrganization().getId(),
                user.getRole().name()
        );
    }
}