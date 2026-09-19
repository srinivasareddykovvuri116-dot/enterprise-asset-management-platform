package com.enterprise.assetmanagement.organization;

import com.enterprise.assetmanagement.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    public OrganizationController(
            OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Organization> getMyOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long organizationId = userDetails.getOrganizationId();

        Organization organization =
                organizationRepository.findById(organizationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found"
                                )
                        );

        return ResponseEntity.ok(organization);
    }
}