package benjamin.rest.projects

import benjamin.projects.tasks.api.GetTasksByProjectUuid
import benjamin.projects.tasks.api.Task
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.Tasks
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
import java.util.UUID

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerGetAllByProjectUuidTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val tasksJson = """
        {
          "tasks": [
              {
                "title": "Google-1",
                "assignee": "a.elmurzaev95",
                "status": "IN_PROGRESS",
                "number": 1
              },
              {
                "title": "Google-2",
                "assignee": "islam95",
                "status": "ACCEPTANCE",
                "number": 2
              },
              {
                "title": "Google-3",
                "assignee": "m.mansoorov",
                "status": "DONE",
                "number": 3
              }
          ]
        }
    """.trimIndent()

    private val tasks = Tasks(
        tasks = listOf(
            Task(
                number = 1,
                title = "Google-1",
                assignee = "a.elmurzaev95",
                status = TaskStatus.IN_PROGRESS
            ),
            Task(
                number = 2,
                title = "Google-2",
                assignee = "islam95",
                status = TaskStatus.ACCEPTANCE
            ),
            Task(
                number = 3,
                title = "Google-3",
                assignee = "m.mansoorov",
                status = TaskStatus.DONE
            )
        )
    )

    private val uuid = UUID.randomUUID()

    private val currentUser = "adamelmurzaev95"

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.get("/projects/$uuid/tasks")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 404 Not Found when no such project exists`() {
        every { projectModel.getAllTasksByProjectUuid(uuid, currentUser) } returns GetTasksByProjectUuid.ProjectNotFound

        web.get("/projects/$uuid/tasks") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 403 Forbidden when user doesnt have access to this project`() {
        every { projectModel.getAllTasksByProjectUuid(uuid, currentUser) } returns GetTasksByProjectUuid.AccessDenied

        web.get("/projects/$uuid/tasks") {
            mockJwt()
        }.andExpect {
            status {
                isForbidden()
            }
        }
    }

    @Test
    fun `should return 200 OK with empty tasks`() {
        every { projectModel.getAllTasksByProjectUuid(uuid, currentUser) } returns GetTasksByProjectUuid.Success(
            Tasks(
                emptyList()
            )
        )

        web.get("/projects/$uuid/tasks") {
            mockJwt()
        }.andExpect {
            status {
                isOk()
            }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(
                    """
                    {
                      "tasks": []
                    }
                    """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        every { projectModel.getAllTasksByProjectUuid(uuid, currentUser) } returns GetTasksByProjectUuid.Success(tasks)

        web.get("/projects/$uuid/tasks") {
            mockJwt()
        }.andExpect {
            status {
                isOk()
            }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(tasksJson)
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
