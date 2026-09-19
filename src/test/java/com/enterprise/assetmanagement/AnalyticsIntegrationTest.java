package com.enterprise.assetmanagement;

import com.enterprise.assetmanagement.audit.AuditLogRepository;
import com.enterprise.assetmanagement.organization.OrganizationRepository;
import com.enterprise.assetmanagement.project.ProjectRepository;
import com.enterprise.assetmanagement.task.TaskRepository;
import com.enterprise.assetmanagement.user.UserRepository;

import java.time.LocalDate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsIntegrationTest {

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

        auditLogRepository.deleteAll();

        taskRepository.deleteAll();

        projectRepository.deleteAll();

        userRepository.deleteAll();

        organizationRepository.deleteAll();
    }


    // ============================================================
    // 1. REAL TASK ANALYTICS
    // ============================================================

    @Test
    void organizationAdmin_shouldGetCorrectTaskAnalytics()
            throws Exception {

        String adminToken = registerAdmin(
                "analytics-data-admin@test.com",
                "Analytics Data Organization"
        );

        createProject(
                adminToken,
                "Analytics Project"
        );

        createTask(
                adminToken,
                "Backlog Task",
                "HIGH"
        );

        createTask(
                adminToken,
                "In Progress Task",
                "MEDIUM"
        );

        createTask(
                adminToken,
                "Review Task",
                "CRITICAL"
        );

        createTask(
                adminToken,
                "Completed Task",
                "HIGH"
        );

        String dashboard = mockMvc.perform(
                get("/api/analytics/dashboard")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(4))
                .andExpect(jsonPath("$.completedTasks").value(0))
                .andExpect(jsonPath("$.pendingTasks").value(4))
                .andExpect(jsonPath("$.tasksByStatus").exists())
                .andExpect(jsonPath("$.tasksByPriority").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(
                "Dashboard response: " + dashboard
        );
    }


    // ============================================================
    // 2. PRIORITY ANALYTICS
    // ============================================================

    @Test
    void organizationAdmin_shouldGetTasksByPriority()
            throws Exception {

        String adminToken = registerAdmin(
                "priority-data-admin@test.com",
                "Priority Data Organization"
        );

        createProject(
                adminToken,
                "Priority Project"
        );

        createTask(
                adminToken,
                "Low Task",
                "LOW"
        );

        createTask(
                adminToken,
                "Medium Task",
                "MEDIUM"
        );

        createTask(
                adminToken,
                "High Task 1",
                "HIGH"
        );

        createTask(
                adminToken,
                "High Task 2",
                "HIGH"
        );

        createTask(
                adminToken,
                "Critical Task",
                "CRITICAL"
        );

        mockMvc.perform(
                get("/api/analytics/tasks/priority")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LOW").value(1))
                .andExpect(jsonPath("$.MEDIUM").value(1))
                .andExpect(jsonPath("$.HIGH").value(2))
                .andExpect(jsonPath("$.CRITICAL").value(1));
    }


    // ============================================================
    // 3. STATUS ANALYTICS
    // ============================================================

    @Test
    void organizationAdmin_shouldGetTasksByStatus()
            throws Exception {

        String adminToken = registerAdmin(
                "status-data-admin@test.com",
                "Status Data Organization"
        );

        createProject(
                adminToken,
                "Status Project"
        );

        createTask(
                adminToken,
                "Backlog Task",
                "LOW"
        );

        createTask(
                adminToken,
                "Progress Task",
                "MEDIUM"
        );

        createTask(
                adminToken,
                "Review Task",
                "HIGH"
        );

        createTask(
                adminToken,
                "Completed Task",
                "CRITICAL"
        );


        String tasksResponse = mockMvc.perform(
                get("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode tasks =
                objectMapper.readTree(tasksResponse);


        long progressTaskId = 0;

        long reviewTaskId = 0;

        long completedTaskId = 0;


        for (JsonNode task : tasks.get("content")) {

            String title =
                    task.get("title").asText();


            if (title.equals("Progress Task")) {

                progressTaskId =
                        task.get("id").asLong();

            } else if (title.equals("Review Task")) {

                reviewTaskId =
                        task.get("id").asLong();

            } else if (title.equals("Completed Task")) {

                completedTaskId =
                        task.get("id").asLong();
            }
        }


        // Change task statuses

        updateStatus(
                adminToken,
                progressTaskId,
                "IN_PROGRESS"
        );

        updateStatus(
                adminToken,
                reviewTaskId,
                "IN_REVIEW"
        );

        updateStatus(
                adminToken,
                completedTaskId,
                "COMPLETED"
        );


        // Verify status analytics

        mockMvc.perform(
                get("/api/analytics/tasks/status")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BACKLOG").value(1))
                .andExpect(jsonPath("$.IN_PROGRESS").value(1))
                .andExpect(jsonPath("$.IN_REVIEW").value(1))
                .andExpect(jsonPath("$.COMPLETED").value(1));


        // Verify completed count

        mockMvc.perform(
                get("/api/analytics/tasks/completed")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedTasks").value(1));
    }


    // ============================================================
    // 4. TENANT ISOLATION
    // ============================================================

    @Test
    void organizationAdmin_shouldNotSeeAnotherOrganizationsTasks()
            throws Exception {

        String organizationAToken = registerAdmin(
                "tenant-a-admin@test.com",
                "Organization A"
        );

        String organizationBToken = registerAdmin(
                "tenant-b-admin@test.com",
                "Organization B"
        );


        createProject(
                organizationAToken,
                "Project A"
        );

        createProject(
                organizationBToken,
                "Project B"
        );


        createTask(
                organizationAToken,
                "Organization A Task",
                "HIGH"
        );

        createTask(
                organizationBToken,
                "Organization B Task",
                "CRITICAL"
        );


        // Organization A sees only its task

        mockMvc.perform(
                get("/api/analytics/tasks/total")
                        .header(
                                "Authorization",
                                "Bearer " + organizationAToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(1));


        // Organization B sees only its task

        mockMvc.perform(
                get("/api/analytics/tasks/total")
                        .header(
                                "Authorization",
                                "Bearer " + organizationBToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(1));
    }


    // ============================================================
    // 5. RBAC - TEAM MEMBER DENIED
    // ============================================================

    @Test
    void teamMember_shouldNotAccessAnalytics()
            throws Exception {

        String adminToken = registerAdmin(
                "member-analytics-admin@test.com",
                "Member Analytics Organization"
        );


        String memberRequest = """
                {
                    "email": "analytics-member@test.com",
                    "password": "Password123",
                    "fullName": "Team Member",
                    "role": "TEAM_MEMBER"
                }
                """;


        mockMvc.perform(
                post("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberRequest)
        )
                .andExpect(status().isCreated());


        String memberResponse = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "analytics-member@test.com",
                                    "password": "Password123"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        String memberToken =
                extractToken(memberResponse);


        mockMvc.perform(
                get("/api/analytics/tasks/total")
                        .header(
                                "Authorization",
                                "Bearer " + memberToken
                        )
        )
                .andExpect(status().isForbidden());
    }


    // ============================================================
    // 6. UNAUTHENTICATED ACCESS
    // ============================================================

    @Test
    void unauthenticatedUser_shouldNotAccessAnalytics()
            throws Exception {

        mockMvc.perform(
                get("/api/analytics/tasks/total")
        )
                .andExpect(status().isUnauthorized());
    }


    // ============================================================
    // 7. EMPTY ORGANIZATION TOTAL
    // ============================================================

    @Test
    void organizationAdmin_shouldAccessTotalTasks()
            throws Exception {

        String token = registerAdmin(
                "analytics-admin@test.com",
                "Analytics Organization"
        );


        mockMvc.perform(
                get("/api/analytics/tasks/total")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(0));
    }


    // ============================================================
    // 8. EMPTY DASHBOARD
    // ============================================================

    @Test
    void organizationAdmin_shouldAccessDashboard()
            throws Exception {

        String token = registerAdmin(
                "dashboard-admin@test.com",
                "Dashboard Organization"
        );


        mockMvc.perform(
                get("/api/analytics/dashboard")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.completedTasks").value(0))
                .andExpect(jsonPath("$.pendingTasks").value(0))
                .andExpect(jsonPath("$.tasksByStatus").exists())
                .andExpect(jsonPath("$.tasksByPriority").exists());
    }


    // ============================================================
    // 9. EMPTY STATUS
    // ============================================================

    @Test
    void organizationAdmin_shouldGetEmptyTasksByStatus()
            throws Exception {

        String token = registerAdmin(
                "empty-status-admin@test.com",
                "Empty Status Organization"
        );


        mockMvc.perform(
                get("/api/analytics/tasks/status")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BACKLOG").value(0))
                .andExpect(jsonPath("$.IN_PROGRESS").value(0))
                .andExpect(jsonPath("$.IN_REVIEW").value(0))
                .andExpect(jsonPath("$.COMPLETED").value(0));
    }


    // ============================================================
    // 10. EMPTY PRIORITY
    // ============================================================

    @Test
    void organizationAdmin_shouldGetEmptyTasksByPriority()
            throws Exception {

        String token = registerAdmin(
                "empty-priority-admin@test.com",
                "Empty Priority Organization"
        );


        mockMvc.perform(
                get("/api/analytics/tasks/priority")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LOW").value(0))
                .andExpect(jsonPath("$.MEDIUM").value(0))
                .andExpect(jsonPath("$.HIGH").value(0))
                .andExpect(jsonPath("$.CRITICAL").value(0));
    }


    // ============================================================
    // 11. EMPTY COMPLETED COUNT
    // ============================================================

    @Test
    void organizationAdmin_shouldGetEmptyCompletedTasks()
            throws Exception {

        String token = registerAdmin(
                "empty-completed-admin@test.com",
                "Empty Completed Organization"
        );


        mockMvc.perform(
                get("/api/analytics/tasks/completed")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedTasks").value(0));
    }


    // ============================================================
    // 12. PROJECT MANAGER ACCESS
    // ============================================================

    @Test
    void projectManager_shouldAccessAnalytics()
            throws Exception {

        String adminToken = registerAdmin(
                "pm-admin@test.com",
                "PM Analytics Organization"
        );


        String managerRequest = """
                {
                    "email": "pm-analytics@test.com",
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


        String managerResponse = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "pm-analytics@test.com",
                                    "password": "Password123"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        String managerToken =
                extractToken(managerResponse);


        mockMvc.perform(
                get("/api/analytics/tasks/total")
                        .header(
                                "Authorization",
                                "Bearer " + managerToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(0));
    }


    // ============================================================
    // 13. OVERDUE TASK ANALYTICS
    // ============================================================

    @Test
    void organizationAdmin_shouldGetCorrectOverdueTaskCount()
            throws Exception {

        String adminToken = registerAdmin(
                "overdue-admin@test.com",
                "Overdue Organization"
        );

        createProject(
                adminToken,
                "Overdue Project"
        );


        // Past due + BACKLOG → overdue
        createTask(
                adminToken,
                "Overdue Backlog Task",
                "LOW",
                LocalDate.now().minusDays(5)
        );


        // Past due + IN_PROGRESS → overdue
        long inProgressTaskId = createTask(
                adminToken,
                "Overdue In Progress Task",
                "HIGH",
                LocalDate.now().minusDays(3)
        );


        updateStatus(
                adminToken,
                inProgressTaskId,
                "IN_PROGRESS"
        );


        // Future due date → not overdue
        createTask(
                adminToken,
                "Future Task",
                "MEDIUM",
                LocalDate.now().plusDays(5)
        );


        // No due date → not overdue
        createTask(
                adminToken,
                "No Due Date Task",
                "LOW",
                null
        );


        mockMvc.perform(
                get("/api/analytics/tasks/overdue")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdueTasks").value(2));
    }


    // ============================================================
    // 14. COMPLETED OVERDUE TASK SHOULD NOT COUNT
    // ============================================================

    @Test
    void completedOverdueTask_shouldNotBeCountedAsOverdue()
            throws Exception {

        String adminToken = registerAdmin(
                "completed-overdue@test.com",
                "Completed Overdue Organization"
        );


        createProject(
                adminToken,
                "Completed Project"
        );


        long taskId = createTask(
                adminToken,
                "Completed Overdue Task",
                "HIGH",
                LocalDate.now().minusDays(10)
        );


        updateStatus(
                adminToken,
                taskId,
                "COMPLETED"
        );


        mockMvc.perform(
                get("/api/analytics/tasks/overdue")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdueTasks").value(0));
    }


    // ============================================================
    // 15. OVERDUE TASK TENANT ISOLATION
    // ============================================================

    @Test
    void organizationAdmin_shouldNotCountOtherOrganizationsOverdueTasks()
            throws Exception {

        String organizationOneToken = registerAdmin(
                "tenant-one-overdue@test.com",
                "Tenant One Overdue"
        );


        createProject(
                organizationOneToken,
                "Tenant One Project"
        );


        createTask(
                organizationOneToken,
                "Tenant One Overdue Task",
                "HIGH",
                LocalDate.now().minusDays(5)
        );


        String organizationTwoToken = registerAdmin(
                "tenant-two-overdue@test.com",
                "Tenant Two Overdue"
        );


        createProject(
                organizationTwoToken,
                "Tenant Two Project"
        );


        createTask(
                organizationTwoToken,
                "Tenant Two Overdue Task",
                "HIGH",
                LocalDate.now().minusDays(5)
        );


        // Organization One sees only its own overdue task

        mockMvc.perform(
                get("/api/analytics/tasks/overdue")
                        .header(
                                "Authorization",
                                "Bearer " + organizationOneToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdueTasks").value(1));


        // Organization Two sees only its own overdue task

        mockMvc.perform(
                get("/api/analytics/tasks/overdue")
                        .header(
                                "Authorization",
                                "Bearer " + organizationTwoToken
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdueTasks").value(1));
    }


    // ============================================================
    // HELPER: REGISTER ADMIN
    // ============================================================

    private String registerAdmin(
            String email,
            String organizationName)
            throws Exception {

        String request = """
                {
                    "email": "%s",
                    "password": "Password123",
                    "fullName": "Analytics Admin",
                    "organizationName": "%s"
                }
                """.formatted(
                email,
                organizationName
        );


        String response = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.organizationId").exists())
                .andExpect(
                        jsonPath("$.role")
                                .value("ORGANIZATION_ADMIN")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();


        return extractToken(response);
    }


    // ============================================================
    // HELPER: CREATE PROJECT
    // ============================================================

    private void createProject(
            String token,
            String projectName)
            throws Exception {


        String managerEmail =
                projectName
                        .replace(" ", "")
                        .toLowerCase()
                        + "-manager@test.com";


        String managerRequest = """
                {
                    "email": "%s",
                    "password": "Password123",
                    "fullName": "Project Manager",
                    "role": "PROJECT_MANAGER"
                }
                """.formatted(managerEmail);


        mockMvc.perform(
                post("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerRequest)
        )
                .andExpect(status().isCreated());


        String loginResponse = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "Password123"
                                }
                                """.formatted(managerEmail))
        )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode managerJson =
                objectMapper.readTree(loginResponse);


        long managerId =
                managerJson
                        .get("userId")
                        .asLong();


        String projectRequest = """
                {
                    "name": "%s",
                    "description": "Analytics test project",
                    "managerId": %d
                }
                """.formatted(
                projectName,
                managerId
        );


        /*
         * ProjectController currently returns HTTP 200.
         */
        mockMvc.perform(
                post("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequest)
        )
                .andExpect(status().isOk());
    }


    // ============================================================
    // HELPER: CREATE TASK WITHOUT DUE DATE
    // ============================================================

    private long createTask(
            String token,
            String title,
            String priority)
            throws Exception {

        return createTask(
                token,
                title,
                priority,
                null
        );
    }


    // ============================================================
    // HELPER: CREATE TASK WITH DUE DATE
    // ============================================================

    private long createTask(
            String token,
            String title,
            String priority,
            LocalDate dueDate)
            throws Exception {


        String projectsResponse = mockMvc.perform(
                get("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode projects =
                objectMapper.readTree(projectsResponse);


        /*
         * Current /api/projects response is expected
         * to be a JSON array.
         */
        long projectId =
                projects
                        .get(0)
                        .get("id")
                        .asLong();


        String dueDateJson =
                dueDate == null
                        ? "null"
                        : "\"" + dueDate + "\"";


        String request = """
                {
                    "title": "%s",
                    "description": "Analytics test task",
                    "priority": "%s",
                    "projectId": %d,
                    "dueDate": %s
                }
                """.formatted(
                title,
                priority,
                projectId,
                dueDateJson
        );


        String response = mockMvc.perform(
                post("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode taskJson =
                objectMapper.readTree(response);


        return taskJson
                .get("id")
                .asLong();
    }


    // ============================================================
    // HELPER: UPDATE TASK STATUS
    // ============================================================

    private void updateStatus(
            String token,
            long taskId,
            String status)
            throws Exception {


        mockMvc.perform(
                patch(
                        "/api/tasks/"
                                + taskId
                                + "/status"
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "%s"
                                }
                                """.formatted(status))
        )
                .andExpect(status().isOk());
    }


    // ============================================================
    // HELPER: EXTRACT JWT
    // ============================================================

    private String extractToken(
            String response)
            throws Exception {

        JsonNode json =
                objectMapper.readTree(response);

        return json
                .get("token")
                .asText();
    }
}