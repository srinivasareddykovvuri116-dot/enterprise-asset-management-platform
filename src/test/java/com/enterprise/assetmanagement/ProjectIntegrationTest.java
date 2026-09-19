package com.enterprise.assetmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.enterprise.assetmanagement.audit.AuditAction;
import com.enterprise.assetmanagement.audit.AuditLog;
import com.enterprise.assetmanagement.audit.AuditLogRepository;
import com.enterprise.assetmanagement.organization.OrganizationRepository;
import com.enterprise.assetmanagement.project.Project;
import com.enterprise.assetmanagement.project.ProjectRepository;
import com.enterprise.assetmanagement.task.TaskRepository;
import com.enterprise.assetmanagement.user.UserRepository;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void organizationAdmin_shouldCreateProject() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectRequest = """
                {
                    "name": "Enterprise Project",
                    "description": "Test enterprise project",
                    "managerId": %d
                }
                """.formatted(adminId);

        mockMvc.perform(
                post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Enterprise Project"))
                .andExpect(jsonPath("$.description")
                        .value("Test enterprise project"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$.managerId")
                        .value(adminId))
                .andExpect(jsonPath("$.managerName")
                        .value("Test Admin"));
    }

    @Test
    void authenticatedUser_shouldGetProjects() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        createProject(token, adminId, "Project One");

        mockMvc.perform(
                get("/api/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Project One"))
                .andExpect(jsonPath("$[0].managerId")
                        .value(adminId));
    }

    @Test
    void authenticatedUser_shouldGetProjectById() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Project One");

        Long projectId = extractLong(projectResponse, "id");

        mockMvc.perform(
                get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(projectId))
                .andExpect(jsonPath("$.name")
                        .value("Project One"))
                .andExpect(jsonPath("$.organizationId")
                        .isNumber())
                .andExpect(jsonPath("$.managerId")
                        .value(adminId));
    }

    @Test
    void organizationAdmin_shouldUpdateProject() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Old Project");

        Long projectId = extractLong(projectResponse, "id");

        String updateRequest = """
                {
                    "name": "Updated Project",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(
                put("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(projectId))
                .andExpect(jsonPath("$.name")
                        .value("Updated Project"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"));
    }

    @Test
    void organizationAdmin_shouldArchiveProject() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Archive Project");

        Long projectId = extractLong(projectResponse, "id");

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(projectId))
                .andExpect(jsonPath("$.status")
                        .value("ARCHIVED"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow();

        assertEquals(
                "ARCHIVED",
                project.getStatus().name()
        );
    }

    @Test
    void organizationAdmin_shouldRestoreProject() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Restore Project");

        Long projectId = extractLong(projectResponse, "id");

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("ARCHIVED"));

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(projectId))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow();

        assertEquals(
                "ACTIVE",
                project.getStatus().name()
        );
    }

    @Test
    void organizationAdmin_shouldCreateProjectAuditLog()
            throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        createProject(token, adminId, "Audited Project");

        List<AuditLog> auditLogs =
                auditLogRepository.findAll();

        assertTrue(
                auditLogs.stream()
                        .anyMatch(log ->
                                log.getAction()
                                        == AuditAction.PROJECT_CREATED),
                "PROJECT_CREATED audit log should exist"
        );
    }

    @Test
    void archiveProject_shouldCreateAuditLog()
            throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Archive Audit Project");

        Long projectId = extractLong(projectResponse, "id");

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        List<AuditLog> auditLogs =
                auditLogRepository.findAll();

        assertTrue(
                auditLogs.stream()
                        .anyMatch(log ->
                                log.getAction()
                                        == AuditAction.PROJECT_ARCHIVED),
                "PROJECT_ARCHIVED audit log should exist"
        );
    }

    @Test
    void restoreProject_shouldCreateAuditLog()
            throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Restore Audit Project");

        Long projectId = extractLong(projectResponse, "id");

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        List<AuditLog> auditLogs =
                auditLogRepository.findAll();

        assertTrue(
                auditLogs.stream()
                        .anyMatch(log ->
                                log.getAction()
                                        == AuditAction.PROJECT_UPDATED
                                && log.getDetails() != null
                                && log.getDetails()
                                        .contains("Restored project")),
                "PROJECT_UPDATED restore audit log should exist"
        );
    }

    @Test
    void cannotCreateDuplicateProjectNameInSameOrganization()
            throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        createProject(
                token,
                adminId,
                "Duplicate Project"
        );

        String duplicateRequest = """
                {
                    "name": "Duplicate Project",
                    "description": "Duplicate",
                    "managerId": %d
                }
                """.formatted(adminId);

        mockMvc.perform(
                post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Project name already exists in this organization"
                        ));
    }

    @Test
    void cannotGetNonExistingProject() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");

        mockMvc.perform(
                get("/api/projects/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Project not found"));
    }

    @Test
    void cannotArchiveAlreadyArchivedProject()
            throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Already Archived");

        Long projectId = extractLong(projectResponse, "id");

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Project is already archived"));
    }

    @Test
    void cannotRestoreActiveProject() throws Exception {

        String registrationResponse = registerAdmin();

        String token = extractString(registrationResponse, "token");
        Long adminId = extractLong(registrationResponse, "userId");

        String projectResponse =
                createProject(token, adminId, "Active Project");

        Long projectId = extractLong(projectResponse, "id");

        mockMvc.perform(
                patch("/api/projects/" + projectId + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Only archived projects can be restored"));
    }

    private String registerAdmin() throws Exception {

        String registerRequest = """
                {
                    "email": "admin@test.com",
                    "password": "password123",
                    "fullName": "Test Admin",
                    "organizationName": "Test Organization"
                }
                """;

        MvcResult result = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String createProject(
            String token,
            Long managerId,
            String projectName) throws Exception {

        String projectRequest = """
                {
                    "name": "%s",
                    "description": "Test project description",
                    "managerId": %d
                }
                """.formatted(projectName, managerId);

        MvcResult result = mockMvc.perform(
                post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequest))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String extractString(
            String json,
            String field) {

        String search = "\"" + field + "\":\"";

        int start = json.indexOf(search);

        if (start == -1) {
            throw new IllegalArgumentException(
                    "Field not found: " + field
            );
        }

        start += search.length();

        int end = json.indexOf("\"", start);

        return json.substring(start, end);
    }

    private Long extractLong(
            String json,
            String field) {

        String search = "\"" + field + "\":";

        int start = json.indexOf(search);

        if (start == -1) {
            throw new IllegalArgumentException(
                    "Field not found: " + field
            );
        }

        start += search.length();

        while (start < json.length()
                && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;

        while (end < json.length()
                && Character.isDigit(json.charAt(end))) {
            end++;
        }

        return Long.parseLong(
                json.substring(start, end)
        );
    }
}