package benjamin.rest.projects

import benjamin.projects.tasks.api.Task
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.Tasks
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
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerGetAllByProjectTitleTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
    private lateinit var projectModel: ProjectModel

    private val tasksJson = """
        {
          "tasks": [
              {
                "id": 1,
                "title": "Google-1",
                "assignee": "a.elmurzaev95",
                "status": "IN_PROGRESS"
              },
              {
                "id": 2,
                "title": "Google-2",
                "assignee": "islam95",
                "status": "ACCEPTANCE"
              },
              {
                "id": 5,
                "title": "Google-3",
                "assignee": "m.mansoorov",
                "status": "DONE"
              }
          ]
        }
    """.trimIndent()

    private val tasks = Tasks(
        tasks = listOf(
            Task(
                id = 1,
                title = "Google-1",
                assignee = "a.elmurzaev95",
                status = TaskStatus.IN_PROGRESS
            ),
            Task(
                id = 2,
                title = "Google-2",
                assignee = "islam95",
                status = TaskStatus.ACCEPTANCE
            ),
            Task(
                id = 5,
                title = "Google-3",
                assignee = "m.mansoorov",
                status = TaskStatus.DONE
            )
        )
    )

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.get("/projects/Google/tasks")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 404 Not Found when no such project exists`() {
        web.get("/projects/Google/tasks") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 200 OK with empty tasks`() {
        Mockito.`when`(projectModel.getAllTasksByProjectTitle("Google"))
            .thenReturn(Tasks(emptyList()))

        web.get("/projects/Google/tasks") {
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
        Mockito.`when`(projectModel.getAllTasksByProjectTitle("Google"))
            .thenReturn(tasks)

        web.get("/projects/Google/tasks") {
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
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}
