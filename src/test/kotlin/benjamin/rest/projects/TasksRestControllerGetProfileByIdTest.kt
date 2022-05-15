package benjamin.rest.projects

import benjamin.projects.tasks.api.TaskProfile
import benjamin.projects.tasks.api.TaskStatus
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
import java.time.Instant

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerGetProfileByIdTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
    private lateinit var projectModel: ProjectModel

    private val taskProfileJson = """
        {
          "id": 1,
          "title": "Google-1",
          "description": "Create project in gitlab",
          "projectTitle": "Google",
          "author": "ivan_andrianov",
          "assignee": "a.elmurzaev95",
          "creationDateTime": "2022-05-03T09:36:00Z",
          "changedDateTime": "2022-05-03T09:36:00Z",
          "status": "DONE"
        }
    """.trimIndent()

    private val taskProfile = TaskProfile(
        id = 1,
        title = "Google-1",
        description = "Create project in gitlab",
        projectTitle = "Google",
        author = "ivan_andrianov",
        assignee = "a.elmurzaev95",
        creationDateTime = Instant.parse("2022-05-03T09:36:00Z"),
        changedDateTime = Instant.parse("2022-05-03T09:36:00Z"),
        status = TaskStatus.DONE
    )

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.get("/projects/Google/tasks/1")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 404 Not Found when there no task with such id`() {
        web.get("/projects/Google/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 200 OK with correct body`() {
        Mockito.`when`(projectModel.getTaskProfileById(1))
            .thenReturn(taskProfile)

        web.get("/projects/Google/tasks/1") {
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
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}
