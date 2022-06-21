package benjamin.rest.projects

import benjamin.projects.tasks.api.GetTaskProfileByNumber
import benjamin.projects.tasks.api.TaskProfile
import benjamin.projects.tasks.api.TaskStatus
import benjamin.rest.models.ProjectModel
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
import org.springframework.test.web.servlet.get
import java.time.Instant
import java.util.UUID

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerGetProfileByUuidTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val taskProfileJson = """
        {
          "title": "Google-1",
          "description": "Create project in gitlab",
          "projectTitle": "Google",
          "author": "ivan_andrianov",
          "assignee": "a.elmurzaev95",
          "creationDateTime": "2022-05-03T09:36:00Z",
          "changedDateTime": "2022-05-03T09:36:00Z",
          "status": "DONE",
          "number": 1
        }
    """.trimIndent()

    private val taskProfile = TaskProfile(
        number = 1,
        title = "Google-1",
        description = "Create project in gitlab",
        projectTitle = "Google",
        author = "ivan_andrianov",
        assignee = "a.elmurzaev95",
        creationDateTime = Instant.parse("2022-05-03T09:36:00Z"),
        changedDateTime = Instant.parse("2022-05-03T09:36:00Z"),
        status = TaskStatus.DONE
    )

    private val uuid = UUID.randomUUID()

    private val currentUser = "adamelmurzaev95"

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.get("/projects/$uuid/tasks/1")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 404 Not Found when there no task with such number`() {
        every { projectModel.getTaskProfileByNumber(1, uuid, currentUser) } returns GetTaskProfileByNumber.TaskNotFound

        web.get("/projects/$uuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 404 Not Found when there no project with such uuid`() {
        every { projectModel.getTaskProfileByNumber(1, uuid, currentUser) } returns GetTaskProfileByNumber.ProjectNotFound

        web.get("/projects/$uuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 403 Forbidden when user doesnt have access to this project`() {
        every { projectModel.getTaskProfileByNumber(1, uuid, currentUser) } returns GetTaskProfileByNumber.AccessDenied

        web.get("/projects/$uuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isForbidden()
            }
        }
    }

    @Test
    fun `should return 200 OK with correct body`() {
        every { projectModel.getTaskProfileByNumber(1, uuid, currentUser) } returns GetTaskProfileByNumber.Success(
            taskProfile
        )

        web.get("/projects/$uuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                content { json(taskProfileJson) }
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
