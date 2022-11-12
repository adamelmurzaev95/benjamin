package benjamin.rest.projects

import benjamin.projects.api.Project
import benjamin.rest.projects.models.ProjectModel
import benjamin.security.Oauth2SecurityConfig
import benjamin.security.ProjectAccessDeniedException
import benjamin.security.ProjectNotFoundException
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerGetByUuidTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val projectJson = """
        {
          "title": "Google",
          "description": "American multinational technology company",
          "author": "adamelmurzaev95"
        }
    """.trimIndent()

    private val uuid = UUID.randomUUID()

    private val currentUser = "adamelmurzaev95"

    private val project = Project(
        uuid = uuid,
        title = "Google",
        description = "American multinational technology company",
        author = currentUser
    )

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.get("/projects/$uuid")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 404 Not Found when no project with such uuid`() {
        every { projectModel.getProjectByUuid(uuid, currentUser) } throws ProjectNotFoundException("Project not found")

        web.get("/projects/$uuid") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 403 Forbidden when user doesnt have access to this project`() {
        every { projectModel.getProjectByUuid(uuid, currentUser) } throws ProjectAccessDeniedException("Access denied")

        web.get("/projects/$uuid") {
            mockJwt()
        }.andExpect {
            status {
                isForbidden()
            }
        }
    }

    @Test
    fun `should return 200 OK with correct body`() {
        every { projectModel.getProjectByUuid(uuid, currentUser) } returns project

        web.get("/projects/$uuid") {
            mockJwt()
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(projectJson)
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(jwt().jwt { it.claim("user_name", currentUser) })
}
