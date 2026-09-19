package com.enterprise.assetmanagement;

import com.enterprise.assetmanagement.audit.AuditAction;
import com.enterprise.assetmanagement.audit.AuditLogRepository;
import com.enterprise.assetmanagement.organization.OrganizationRepository;
import com.enterprise.assetmanagement.project.ProjectRepository;
import com.enterprise.assetmanagement.task.TaskRepository;
import com.enterprise.assetmanagement.user.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;


    @BeforeEach
    void cleanDatabase() {

        taskRepository.deleteAll();

        auditLogRepository.deleteAll();

        projectRepository.deleteAll();

        userRepository.deleteAll();

        organizationRepository.deleteAll();
    }


    // ============================================================
    // CREATE TASK
    // ============================================================

    @Test
    void organizationAdmin_shouldCreateTask() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Task Test Project"
        );

        Map<String, Object> request = new HashMap<>();

        request.put("title", "Build REST API");

        request.put("description", "Implement task API");

        request.put("priority", "HIGH");

        request.put("projectId", projectId);


        mockMvc.perform(
                post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Build REST API"))
        .andExpect(jsonPath("$.description").value("Implement task API"))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.status").value("BACKLOG"))
        .andExpect(jsonPath("$.projectId").value(projectId))
        .andExpect(
                jsonPath("$.organizationId")
                        .value(admin.get("organizationId").asLong())
        );
    }


    // ============================================================
    // GET TASKS
    // ============================================================

    @Test
    void authenticatedUser_shouldGetTasks() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Get Tasks Project"
        );

        createTask(
                token,
                projectId,
                "Task One",
                "HIGH"
        );

        createTask(
                token,
                projectId,
                "Task Two",
                "MEDIUM"
        );


        mockMvc.perform(
                get("/api/tasks")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(2));
    }


    // ============================================================
    // GET TASK BY ID
    // ============================================================

    @Test
    void authenticatedUser_shouldGetTaskById() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Single Task Project"
        );

        Long taskId = createTask(
                token,
                projectId,
                "Find This Task",
                "HIGH"
        );


        mockMvc.perform(
                get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(taskId))
        .andExpect(jsonPath("$.title").value("Find This Task"))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.status").value("BACKLOG"));
    }


    // ============================================================
    // UPDATE TASK
    // ============================================================

    @Test
    void organizationAdmin_shouldUpdateTask() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Update Task Project"
        );

        Long taskId = createTask(
                token,
                projectId,
                "Original Task",
                "LOW"
        );


        Map<String, Object> request = new HashMap<>();

        request.put("title", "Updated Task");

        request.put("description", "Updated description");

        request.put("priority", "CRITICAL");


        mockMvc.perform(
                put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Task"))
        .andExpect(jsonPath("$.description").value("Updated description"))
        .andExpect(jsonPath("$.priority").value("CRITICAL"));
    }


    // ============================================================
    // UPDATE TASK STATUS
    // ============================================================

    @Test
    void organizationAdmin_shouldUpdateTaskStatus() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Status Project"
        );

        Long taskId = createTask(
                token,
                projectId,
                "Status Task",
                "MEDIUM"
        );


        Map<String, Object> request = Map.of(
                "status",
                "IN_PROGRESS"
        );


        mockMvc.perform(
                patch("/api/tasks/" + taskId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }


    // ============================================================
    // ASSIGN TASK
    // ============================================================

    @Test
    void organizationAdmin_shouldAssignTask() throws Exception {

        JsonNode admin = registerAdmin();

        String adminToken = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                adminToken,
                adminId,
                "Assignment Project"
        );

        JsonNode teamMember = createTeamMember(adminToken);

        Long teamMemberId = teamMember.get("id").asLong();

        Long taskId = createTask(
                adminToken,
                projectId,
                "Assignment Task",
                "HIGH"
        );


        Map<String, Object> request = Map.of(
                "assigneeId",
                teamMemberId
        );


        mockMvc.perform(
                patch("/api/tasks/" + taskId + "/assignee")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assigneeId").value(teamMemberId))
        .andExpect(jsonPath("$.assigneeName").value("Task Member"));
    }


    // ============================================================
    // FILTER BY STATUS
    // ============================================================

    @Test
    void shouldFilterTasksByStatus() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Status Filter Project"
        );

        Long taskId = createTask(
                token,
                projectId,
                "In Progress Task",
                "HIGH"
        );


        Map<String, Object> statusRequest = Map.of(
                "status",
                "IN_PROGRESS"
        );


        mockMvc.perform(
                patch("/api/tasks/" + taskId + "/status")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        statusRequest
                                )
                        )
        )
        .andExpect(status().isOk());


        createTask(
                token,
                projectId,
                "Backlog Task",
                "LOW"
        );


        mockMvc.perform(
                get("/api/tasks")
                        .param("status", "IN_PROGRESS")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(
                jsonPath("$.content[0].title")
                        .value("In Progress Task")
        );
    }


    // ============================================================
    // FILTER BY PRIORITY
    // ============================================================

    @Test
    void shouldFilterTasksByPriority() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Priority Filter Project"
        );


        createTask(
                token,
                projectId,
                "Critical Task",
                "CRITICAL"
        );

        createTask(
                token,
                projectId,
                "Low Task",
                "LOW"
        );


        mockMvc.perform(
                get("/api/tasks")
                        .param("priority", "CRITICAL")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(
                jsonPath("$.content[0].title")
                        .value("Critical Task")
        );
    }


    // ============================================================
    // PAGINATION
    // ============================================================

    @Test
    void shouldSupportPagination() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Pagination Project"
        );


        createTask(
                token,
                projectId,
                "Task 1",
                "LOW"
        );

        createTask(
                token,
                projectId,
                "Task 2",
                "LOW"
        );

        createTask(
                token,
                projectId,
                "Task 3",
                "LOW"
        );


        mockMvc.perform(
                get("/api/tasks")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(2));
    }


    // ============================================================
    // ARCHIVED PROJECT
    // ============================================================

    @Test
    void shouldNotCreateTaskInArchivedProject() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Archived Project"
        );


        mockMvc.perform(
                patch("/api/projects/" + projectId + "/archive")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk());


        Map<String, Object> request = new HashMap<>();

        request.put("title", "Should Fail");

        request.put("description", "Cannot create");

        request.put("priority", "HIGH");

        request.put("projectId", projectId);


        mockMvc.perform(
                post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }


    // ============================================================
    // TENANT ISOLATION
    // ============================================================

    @Test
    void userShouldNotAccessTaskFromAnotherOrganization()
            throws Exception {

        JsonNode adminOne = registerAdmin();

        String tokenOne = adminOne.get("token").asText();

        Long adminIdOne = adminOne.get("userId").asLong();

        Long projectId = createProject(
                tokenOne,
                adminIdOne,
                "Tenant One Project"
        );


        Long taskId = createTask(
                tokenOne,
                projectId,
                "Tenant One Task",
                "HIGH"
        );


        JsonNode adminTwo = registerAdmin(
                "admin2@test.com",
                "Organization Two"
        );

        String tokenTwo = adminTwo.get("token").asText();


        mockMvc.perform(
                get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenTwo)
        )
        .andExpect(status().isBadRequest());
    }


    // ============================================================
    // TEAM MEMBER CANNOT ASSIGN
    // ============================================================

    @Test
    void teamMember_shouldNotAssignTask() throws Exception {

        JsonNode admin = registerAdmin();

        String adminToken = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                adminToken,
                adminId,
                "RBAC Assignment Project"
        );


        JsonNode teamMember = createTeamMember(adminToken);

        String teamToken = login(
                "member@test.com",
                "password123"
        );


        Long taskId = createTask(
                adminToken,
                projectId,
                "RBAC Task",
                "MEDIUM"
        );


        Map<String, Object> request = Map.of(
                "assigneeId",
                teamMember.get("id").asLong()
        );


        mockMvc.perform(
                patch("/api/tasks/" + taskId + "/assignee")
                        .header(
                                "Authorization",
                                "Bearer " + teamToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isForbidden());
    }


    // ============================================================
    // TEAM MEMBER CANNOT PERFORM FULL TASK UPDATE
    // ============================================================

    @Test
    void teamMember_shouldNotPerformFullTaskUpdate()
            throws Exception {

        JsonNode admin = registerAdmin();

        String adminToken = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                adminToken,
                adminId,
                "Team Member Project"
        );


        JsonNode teamMember = createTeamMember(adminToken);

        Long teamMemberId = teamMember.get("id").asLong();


        Long taskId = createTask(
                adminToken,
                projectId,
                "Own Task",
                "MEDIUM"
        );


        Map<String, Object> assignRequest = Map.of(
                "assigneeId",
                teamMemberId
        );


        mockMvc.perform(
                patch("/api/tasks/" + taskId + "/assignee")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        assignRequest
                                )
                        )
        )
        .andExpect(status().isOk());


        String teamToken = login(
                "member@test.com",
                "password123"
        );


        Map<String, Object> updateRequest = Map.of(
                "title",
                "Updated By Team Member",
                "description",
                "Own assigned task",
                "priority",
                "HIGH"
        );


        /*
         * TEAM_MEMBER is intentionally forbidden from performing
         * full task updates.
         *
         * Full task updates are restricted to:
         * - ORGANIZATION_ADMIN
         * - PROJECT_MANAGER
         *
         * TEAM_MEMBER can use the dedicated status endpoint:
         * PATCH /api/tasks/{taskId}/status
         */

        mockMvc.perform(
                put("/api/tasks/" + taskId)
                        .header(
                                "Authorization",
                                "Bearer " + teamToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        updateRequest
                                )
                        )
        )
        .andExpect(status().isForbidden());
    }


    // ============================================================
    // TASK CREATED AUDIT
    // ============================================================

    @Test
    void createTask_shouldCreateAuditLog() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Audit Project"
        );


        Long taskId = createTask(
                token,
                projectId,
                "Audited Task",
                "HIGH"
        );


        boolean exists = auditLogRepository.findAll()
                .stream()
                .anyMatch(log ->
                        log.getAction() == AuditAction.TASK_CREATED
                                && log.getEntityId().equals(taskId)
                );


        assertTrue(exists);
    }


    // ============================================================
    // STATUS CHANGE AUDIT
    // ============================================================

    @Test
    void statusChange_shouldCreateAuditLog() throws Exception {

        JsonNode admin = registerAdmin();

        String token = admin.get("token").asText();

        Long adminId = admin.get("userId").asLong();

        Long projectId = createProject(
                token,
                adminId,
                "Status Audit Project"
        );


        Long taskId = createTask(
                token,
                projectId,
                "Status Audit Task",
                "MEDIUM"
        );


        Map<String, Object> request = Map.of(
                "status",
                "COMPLETED"
        );


        mockMvc.perform(
                patch("/api/tasks/" + taskId + "/status")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk());


        boolean exists = auditLogRepository.findAll()
                .stream()
                .anyMatch(log ->
                        log.getAction()
                                == AuditAction.TASK_STATUS_CHANGED
                                && log.getEntityId().equals(taskId)
                );


        assertTrue(exists);
    }


    // ============================================================
    // HELPERS
    // ============================================================

    private JsonNode registerAdmin() throws Exception {

        return registerAdmin(
                "admin@test.com",
                "Test Organization"
        );
    }


    private JsonNode registerAdmin(
            String email,
            String organizationName) throws Exception {

        Map<String, Object> request = new HashMap<>();

        request.put("email", email);

        request.put("password", "password123");

        request.put("fullName", "Test Admin");

        request.put("organizationName", organizationName);


        String response = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();


        return objectMapper.readTree(response);
    }


    private JsonNode createTeamMember(
            String adminToken) throws Exception {

        Map<String, Object> request = new HashMap<>();

        request.put("email", "member@test.com");

        request.put("password", "password123");

        request.put("fullName", "Task Member");

        request.put("role", "TEAM_MEMBER");


        String response = mockMvc.perform(
                post("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();


        return objectMapper.readTree(response);
    }


    private String login(
            String email,
            String password) throws Exception {

        Map<String, Object> request = Map.of(
                "email", email,
                "password", password
        );


        String response = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();


        return objectMapper.readTree(response)
                .get("token")
                .asText();
    }


    private Long createProject(
            String token,
            Long managerId,
            String projectName) throws Exception {

        Map<String, Object> request = new HashMap<>();

        request.put("name", projectName);

        request.put(
                "description",
                "Project created for integration testing"
        );

        request.put("managerId", managerId);


        String response = mockMvc.perform(
                post("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();


        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }


    private Long createTask(
            String token,
            Long projectId,
            String title,
            String priority) throws Exception {

        Map<String, Object> request = new HashMap<>();

        request.put("title", title);

        request.put(
                "description",
                "Integration test task"
        );

        request.put("priority", priority);

        request.put("projectId", projectId);


        String response = mockMvc.perform(
                post("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();


        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }
}