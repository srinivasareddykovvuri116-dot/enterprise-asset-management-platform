package com.enterprise.assetmanagement;

import com.enterprise.assetmanagement.audit.AuditLogRepository;
import com.enterprise.assetmanagement.organization.OrganizationRepository;
import com.enterprise.assetmanagement.project.ProjectRepository;
import com.enterprise.assetmanagement.task.TaskRepository;
import com.enterprise.assetmanagement.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // =========================================================
    // DATABASE CLEANUP
    // =========================================================

    @BeforeEach
    void cleanDatabase() {

        // Audit logs reference users and projects.
        // Therefore delete audit logs before users/organizations.
        auditLogRepository.deleteAll();
        taskRepository.deleteAll();

        projectRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    // =========================================================
    // REGISTRATION TEST
    // =========================================================

    @Test
    void register_shouldCreateOrganizationAdmin() throws Exception {

        String request = """
                {
                    "email": "admin@test.com",
                    "password": "Password123",
                    "fullName": "Test Admin",
                    "organizationName": "Test Organization"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.organizationId").exists())
                .andExpect(jsonPath("$.role")
                        .value("ORGANIZATION_ADMIN"));
    }

    // =========================================================
    // DUPLICATE EMAIL TEST
    // =========================================================

    @Test
    void register_shouldRejectDuplicateEmail() throws Exception {

        String firstRequest = """
                {
                    "email": "admin@test.com",
                    "password": "Password123",
                    "fullName": "Test Admin",
                    "organizationName": "Organization One"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        String duplicateRequest = """
                {
                    "email": "admin@test.com",
                    "password": "Password123",
                    "fullName": "Another Admin",
                    "organizationName": "Organization Two"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(duplicateRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));
    }

    // =========================================================
    // DUPLICATE ORGANIZATION TEST
    // =========================================================

    @Test
    void register_shouldRejectDuplicateOrganizationName()
            throws Exception {

        String firstRequest = """
                {
                    "email": "admin1@test.com",
                    "password": "Password123",
                    "fullName": "Admin One",
                    "organizationName": "Same Organization"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        String duplicateOrgRequest = """
                {
                    "email": "admin2@test.com",
                    "password": "Password123",
                    "fullName": "Admin Two",
                    "organizationName": "Same Organization"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(duplicateOrgRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Organization name is already registered"));
    }

    // =========================================================
    // UNAUTHORIZED ACCESS TEST
    // =========================================================

    @Test
    void protectedEndpoint_withoutToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/api/organizations/me")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content().string(
                                containsString("Unauthorized")
                        )
                );
    }

    // =========================================================
    // VALID LOGIN TEST
    // =========================================================

    @Test
    void login_withValidCredentials_shouldReturnToken()
            throws Exception {

        String registerRequest = """
                {
                    "email": "login@test.com",
                    "password": "Password123",
                    "fullName": "Login User",
                    "organizationName": "Login Organization"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequest)
                )
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "login@test.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.organizationId").exists())
                .andExpect(jsonPath("$.role")
                        .value("ORGANIZATION_ADMIN"));
    }

    // =========================================================
    // INVALID PASSWORD TEST
    // =========================================================

    @Test
    void login_withInvalidPassword_shouldReturnUnauthorized()
            throws Exception {

        String registerRequest = """
                {
                    "email": "invalid@test.com",
                    "password": "Password123",
                    "fullName": "Invalid Login",
                    "organizationName": "Invalid Organization"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequest)
                )
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "invalid@test.com",
                    "password": "WrongPassword"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // STEP 60
    // TENANT ISOLATION TEST
    // =========================================================

    @Test
    void organizationA_shouldNotAccessOrganizationBProject()
            throws Exception {

        // -----------------------------------------------------
        // ORGANIZATION A
        // -----------------------------------------------------

        String adminARequest = """
                {
                    "email": "adminA@test.com",
                    "password": "Password123",
                    "fullName": "Admin A",
                    "organizationName": "Organization A"
                }
                """;

        String adminAResponse =
                mockMvc.perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(adminARequest)
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.userId").exists())
                        .andExpect(jsonPath("$.organizationId").exists())
                        .andExpect(jsonPath("$.role")
                                .value("ORGANIZATION_ADMIN"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String tokenA = extractToken(adminAResponse);

        long organizationAId =
                extractLong(
                        adminAResponse,
                        "organizationId"
                );

        long adminAId =
                extractLong(
                        adminAResponse,
                        "userId"
                );

        // -----------------------------------------------------
        // ORGANIZATION B
        // -----------------------------------------------------

        String adminBRequest = """
                {
                    "email": "adminB@test.com",
                    "password": "Password123",
                    "fullName": "Admin B",
                    "organizationName": "Organization B"
                }
                """;

        String adminBResponse =
                mockMvc.perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(adminBRequest)
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.userId").exists())
                        .andExpect(jsonPath("$.organizationId").exists())
                        .andExpect(jsonPath("$.role")
                                .value("ORGANIZATION_ADMIN"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String tokenB = extractToken(adminBResponse);

        long organizationBId =
                extractLong(
                        adminBResponse,
                        "organizationId"
                );

        long adminBId =
                extractLong(
                        adminBResponse,
                        "userId"
                );

        // Organizations must be different.
        assertNotEquals(
                organizationAId,
                organizationBId
        );

        // Users must be different.
        assertNotEquals(
                adminAId,
                adminBId
        );

        // JWTs must be different.
        assertNotEquals(
                tokenA,
                tokenB
        );

        // -----------------------------------------------------
        // CREATE PROJECT IN ORGANIZATION B
        // -----------------------------------------------------

        String projectRequest = """
                {
                    "name": "Organization B Project",
                    "description": "Private project of Organization B",
                    "managerId": %d
                }
                """.formatted(adminBId);

        String projectResponse =
                mockMvc.perform(
                                post("/api/projects")
                                        .header(
                                                "Authorization",
                                                "Bearer " + tokenB
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(projectRequest)
                        )
                        .andExpect(status().isOk())
                        .andExpect(
                                jsonPath("$.organizationId")
                                        .value(organizationBId)
                        )
                        .andExpect(
                                jsonPath("$.name")
                                        .value(
                                                "Organization B Project"
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long projectBId =
                extractLong(
                        projectResponse,
                        "id"
                );

        // -----------------------------------------------------
        // ORGANIZATION B CAN ACCESS ITS OWN PROJECT
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/projects/" + projectBId)
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenB
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(projectBId)
                )
                .andExpect(
                        jsonPath("$.organizationId")
                                .value(organizationBId)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(
                                        "Organization B Project"
                                )
                );

        // -----------------------------------------------------
        // ORGANIZATION A MUST NOT ACCESS B'S PROJECT
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/projects/" + projectBId)
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenA
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Project not found")
                );
    }

    // =========================================================
    // HELPER: EXTRACT JWT TOKEN
    // =========================================================

    private String extractToken(String response) {

        String search = "\"token\":\"";

        int start =
                response.indexOf(search)
                        + search.length();

        int end =
                response.indexOf(
                        "\"",
                        start
                );

        return response.substring(
                start,
                end
        );
    }

    // =========================================================
    // HELPER: EXTRACT LONG JSON VALUE
    // =========================================================

    private long extractLong(
            String response,
            String field) {

        String search =
                "\"" + field + "\":";

        int start =
                response.indexOf(search)
                        + search.length();

        int end = start;

        while (end < response.length()
                && Character.isDigit(
                        response.charAt(end)
                )) {

            end++;
        }

        return Long.parseLong(
                response.substring(
                        start,
                        end
                )
        );
    }


    // =========================================================
        // STEP 61
        // USER TENANT ISOLATION TEST
        // =========================================================

        @Test
        void organizationA_shouldOnlySeeItsOwnUsers()
                throws Exception {

        // -----------------------------------------------------
        // CREATE ORGANIZATION A
        // -----------------------------------------------------

        String adminARequest = """
                {
                        "email": "adminA@test.com",
                        "password": "Password123",
                        "fullName": "Admin A",
                        "organizationName": "Organization A"
                }
                """;

        String adminAResponse =
                mockMvc.perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(adminARequest)
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.organizationId").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String tokenA = extractToken(adminAResponse);

        // -----------------------------------------------------
        // CREATE ORGANIZATION B
        // -----------------------------------------------------

        String adminBRequest = """
                {
                        "email": "adminB@test.com",
                        "password": "Password123",
                        "fullName": "Admin B",
                        "organizationName": "Organization B"
                }
                """;

        String adminBResponse =
                mockMvc.perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(adminBRequest)
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.organizationId").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String tokenB = extractToken(adminBResponse);

        // -----------------------------------------------------
        // ORGANIZATION A GETS ITS USERS
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenA
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email")
                        .value("adminA@test.com"))
                .andExpect(jsonPath("$[0].fullName")
                        .value("Admin A"))
                .andExpect(jsonPath("$[0].organizationId")
                        .value(
                                extractLong(
                                        adminAResponse,
                                        "organizationId"
                                )
                        ))
                .andExpect(jsonPath("$[?(@.email == 'adminB@test.com')]")
                        .isEmpty());

        // -----------------------------------------------------
        // ORGANIZATION B GETS ITS USERS
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenB
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email")
                        .value("adminB@test.com"))
                .andExpect(jsonPath("$[0].fullName")
                        .value("Admin B"))
                .andExpect(jsonPath("$[0].organizationId")
                        .value(
                                extractLong(
                                        adminBResponse,
                                        "organizationId"
                                )
                        ))
                .andExpect(jsonPath("$[?(@.email == 'adminA@test.com')]")
                        .isEmpty());
        }


        @Test
        void onlyOrganizationAdmin_shouldCreateUsers() throws Exception {

        // -----------------------------------------------------
        // REGISTER ORGANIZATION ADMIN
        // -----------------------------------------------------

        String adminRequest = """
                {
                        "email": "rbacadmin@test.com",
                        "password": "Password123",
                        "fullName": "RBAC Admin",
                        "organizationName": "RBAC Organization"
                }
                """;

        String adminResponse =
                mockMvc.perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(adminRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String adminToken = extractToken(adminResponse);

        Long organizationId =
                extractLong(adminResponse, "organizationId");

        // -----------------------------------------------------
        // CREATE PROJECT MANAGER
        // -----------------------------------------------------

        String managerRequest = """
                {
                        "email": "manager@test.com",
                        "password": "Password123",
                        "fullName": "Project Manager",
                        "role": "PROJECT_MANAGER"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(managerRequest)
                )
                .andExpect(status().isCreated());

        // -----------------------------------------------------
        // LOGIN PROJECT MANAGER
        // -----------------------------------------------------

        String managerLogin = """
                {
                        "email": "manager@test.com",
                        "password": "Password123"
                }
                """;

        String managerResponse =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(managerLogin)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String managerToken = extractToken(managerResponse);

        // -----------------------------------------------------
        // PROJECT MANAGER ATTEMPTS TO CREATE USER
        // -----------------------------------------------------

        String newUserRequest = """
                {
                        "email": "blockeduser@test.com",
                        "password": "Password123",
                        "fullName": "Blocked User",
                        "role": "TEAM_MEMBER"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + managerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newUserRequest)
                )
                .andExpect(status().isForbidden());

        // -----------------------------------------------------
        // VERIFY ORGANIZATION WAS NOT POLLUTED
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$[?(@.email == 'blockeduser@test.com')]"
                        ).isEmpty()
                );
        }
    
        
        @Test
        void teamMember_shouldNotCreateUsers() throws Exception {

        String adminRequest = """
                {
                        "email": "admin63a@test.com",
                        "password": "Password123",
                        "fullName": "Admin",
                        "organizationName": "RBAC Org 63A"
                }
                """;

        String adminResponse = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminToken = extractToken(adminResponse);

        String teamMemberRequest = """
                {
                        "email": "member63@test.com",
                        "password": "Password123",
                        "fullName": "Team Member",
                        "role": "TEAM_MEMBER"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(teamMemberRequest)
                )
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                        "email": "member63@test.com",
                        "password": "Password123"
                }
                """;

        String memberResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String memberToken = extractToken(memberResponse);

        String blockedUser = """
                {
                        "email": "blocked63@test.com",
                        "password": "Password123",
                        "fullName": "Blocked User",
                        "role": "TEAM_MEMBER"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header("Authorization", "Bearer " + memberToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(blockedUser)
                )
                .andExpect(status().isForbidden());
        }



        @Test
        void teamMember_shouldNotCreateProject() throws Exception {

        String adminRequest = """
                {
                        "email": "admin63b@test.com",
                        "password": "Password123",
                        "fullName": "Admin",
                        "organizationName": "RBAC Org 63B"
                }
                """;

        String adminResponse = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminToken = extractToken(adminResponse);

        String memberRequest = """
                {
                        "email": "member63b@test.com",
                        "password": "Password123",
                        "fullName": "Member",
                        "role": "TEAM_MEMBER"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(memberRequest)
                )
                .andExpect(status().isCreated());

        String memberLogin = """
                {
                        "email": "member63b@test.com",
                        "password": "Password123"
                }
                """;

        String memberResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(memberLogin)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String memberToken = extractToken(memberResponse);

        String projectRequest = """
                {
                        "name": "Unauthorized Project",
                        "description": "Should be blocked",
                        "managerId": 1
                }
                """;

        mockMvc.perform(
                        post("/api/projects")
                                .header("Authorization", "Bearer " + memberToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(projectRequest)
                )
                .andExpect(status().isForbidden());
        }



        @Test
        void teamMember_shouldNotAccessAnalytics() throws Exception {

        String adminRequest = """
                {
                        "email": "admin63c@test.com",
                        "password": "Password123",
                        "fullName": "Admin",
                        "organizationName": "RBAC Org 63C"
                }
                """;

        String adminResponse = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminToken = extractToken(adminResponse);

        String memberRequest = """
                {
                        "email": "member63c@test.com",
                        "password": "Password123",
                        "fullName": "Member",
                        "role": "TEAM_MEMBER"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(memberRequest)
                )
                .andExpect(status().isCreated());

        String memberLogin = """
                {
                        "email": "member63c@test.com",
                        "password": "Password123"
                }
                """;

        String memberResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(memberLogin)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String memberToken = extractToken(memberResponse);

        mockMvc.perform(
                        get("/api/analytics/tasks/total")
                                .header("Authorization", "Bearer " + memberToken)
                )
                .andExpect(status().isForbidden());
        }


        @Test
        void projectManager_shouldNotAccessAuditLogs() throws Exception {

        String adminRequest = """
                {
                        "email": "admin63d@test.com",
                        "password": "Password123",
                        "fullName": "Admin",
                        "organizationName": "RBAC Org 63D"
                }
                """;

        String adminResponse = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminToken = extractToken(adminResponse);

        String managerRequest = """
                {
                        "email": "manager63d@test.com",
                        "password": "Password123",
                        "fullName": "Manager",
                        "role": "PROJECT_MANAGER"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(managerRequest)
                )
                .andExpect(status().isCreated());

        String managerLogin = """
                {
                        "email": "manager63d@test.com",
                        "password": "Password123"
                }
                """;

        String managerResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(managerLogin)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String managerToken = extractToken(managerResponse);

        mockMvc.perform(
                        get("/api/audit-logs")
                                .header("Authorization", "Bearer " + managerToken)
                )
                .andExpect(status().isForbidden());
        }


        @Test
        void organizationAdmin_shouldAccessAuditLogs() throws Exception {

        String adminToken = registerAdmin(
                "audit-admin@test.com",
                "Audit Organization"
        );

        mockMvc.perform(get("/api/audit-logs")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
        }



        @Test
        void unauthenticatedUser_shouldNotAccessAuditLogs() throws Exception {

            mockMvc.perform(get("/api/audit-logs"))
                    .andExpect(status().isUnauthorized());
        }



        private String registerAdmin(
        String email,
        String organizationName) throws Exception {

    String request = """
            {
                "email": "%s",
                "password": "Password123",
                "fullName": "Audit Admin",
                "organizationName": "%s"
            }
            """.formatted(email, organizationName);

    String response = mockMvc.perform(
                    post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.organizationId").exists())
            .andExpect(jsonPath("$.role")
                    .value("ORGANIZATION_ADMIN"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    return extractToken(response);
}

}