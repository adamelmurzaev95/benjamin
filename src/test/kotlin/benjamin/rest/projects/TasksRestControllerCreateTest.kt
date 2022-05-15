package benjamin.rest.projects

import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.rest.models.ProjectModel
import benjamin.security.Oauth2SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerCreateTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
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

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.post("/projects/Google/tasks")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 400 Bad Request when invalid body provided`() {
        web.post("/projects/Google/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 404 Not Found when project with such title doesnt exist`() {
        Mockito.`when`(projectModel.createTask("adamelmurzaev95", "Google", createTaskCommand))
            .thenReturn(CreateTaskResult.ProjectNotFound)

        web.post("/projects/Google/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 404 Not Found when assignee with such username doesnt exist`() {
        Mockito.`when`(projectModel.createTask("adamelmurzaev95", "Google", createTaskCommand))
            .thenReturn(CreateTaskResult.AssigneeNotFound)

        web.post("/projects/Google/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 200 OK when there no problem`() {
        Mockito.`when`(projectModel.createTask("adamelmurzaev95", "Google", createTaskCommand))
            .thenReturn(CreateTaskResult.Success)

        web.post("/projects/Google/tasks") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createTaskCommandJson
        }.andExpect {
            status { isOk() }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}
