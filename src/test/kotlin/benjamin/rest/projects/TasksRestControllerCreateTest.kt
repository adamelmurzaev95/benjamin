package benjamin.rest.projects

import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.rest.projects.models.ProjectModel
import benjamin.security.Oauth2SecurityConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerCreateTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val createTaskCommandJson = """
        {
            "title": "Google-3",
            "description": "Create project in gitlab",
            "assignee": "m.mansoorov"
        }
    """.trimIndent()

    private val createTaskCommand = CreateTaskCommand(
        title = "Google-3",
        description = "Create project in gitlab",
        assignee = "m.mansoorov"
    )

    private val uuid = UUID.randomUUID()

    private val currentUser = "adamelmurzaev95"

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.post("/projects/$uuid/tasks")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 400 Bad Request when invalid body provided`() {
        web.post("/projects/$uuid/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 404 Not Found when project with such uuid doesnt exist`() {
        every { projectModel.createTask(currentUser, uuid, createTaskCommand) } returns CreateTaskResult.ProjectNotFound

        web.post("/projects/$uuid/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 403 Forbidden when user doesnt have access to this project`() {
        every { projectModel.createTask(currentUser, uuid, createTaskCommand) } returns CreateTaskResult.AccessDenied

        web.post("/projects/$uuid/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `should return 404 Not Found when assignee with such username doesnt exist`() {
        every { projectModel.createTask(currentUser, uuid, createTaskCommand) } returns CreateTaskResult.AssigneeNotFound

        web.post("/projects/$uuid/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 409 Conflict when assignee doesnt have access to this project`() {
        every { projectModel.createTask(currentUser, uuid, createTaskCommand) } returns CreateTaskResult.AssigneeHasNoAccess

        web.post("/projects/$uuid/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `should return 200 OK when there no problem`() {
        every { projectModel.createTask(currentUser, uuid, createTaskCommand) } returns CreateTaskResult.Success(1)

        web.post("/projects/$uuid/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status {
                isOk()
                content {
                    string("1")
                }
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
