package benjamin.rest.projects

import benjamin.projects.api.Project
import benjamin.projects.api.Projects
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
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerGetAllTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val uuid1 = UUID.randomUUID()
    private val uuid2 = UUID.randomUUID()
    private val uuid3 = UUID.randomUUID()

    private val currentUser = "adamelmurzaev95"

    private val projects = Projects(
        projects = listOf(
            Project(
                uuid = uuid1,
                title = "Google",
                description = "American multinational technology company",
                author = currentUser
            ),
            Project(
                uuid = uuid2,
                title = "Benjamin",
                description = "Task tracker",
                author = currentUser
            ),
            Project(
                uuid = uuid3,
                title = "HRMS",
                description = "Staff cycle product",
                author = currentUser
            )
        )
    )

    private val projectJson = """
        {
          "projects": [
            {
              "uuid": "$uuid1",
              "title": "Google",
              "description": "American multinational technology company",
              "author": "$currentUser"
            },
            {
              "uuid": "$uuid2",
              "title": "Benjamin",
              "description": "Task tracker",
              "author": "$currentUser"
            },
            {
              "uuid": "$uuid3",
              "title": "HRMS",
              "description": "Staff cycle product",
              "author": "$currentUser"
            }
          ]
        }
    """.trimIndent()

    private val emptyProjectsJson = """
        {
          "projects": []
        }
    """.trimIndent()

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.get("/projects")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 200 Success with empty list if no projects found`() {
        every { projectModel.getProjectsByUsername(currentUser) } returns Projects(emptyList())

        web.get("/projects") {
            mockJwt()
        }.andExpect {
            status {
                isOk()
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(emptyProjectsJson)
                }
            }
        }
    }

    @Test
    fun `should return 200 Success with correct body`() {
        every { projectModel.getProjectsByUsername(currentUser) } returns projects

        web.get("/projects") {
            mockJwt()
        }.andExpect {
            status {
                isOk()
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(projectJson)
                }
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
