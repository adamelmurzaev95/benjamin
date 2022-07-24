package benjamin.rest.projects

import benjamin.projects.api.CreateProjectCommand
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

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerCreateTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val createProjectCommand = CreateProjectCommand(
        title = "Google",
        description = "American multinational technology company"
    )

    private val createProjectCommandJson = """
        {
          "title": "Google",
          "description": "American multinational technology company"
        }
    """.trimIndent()

    private val currentUser = "adamelmurzaev95"

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.post("/projects") {
            contentType = MediaType.APPLICATION_JSON
            content = createProjectCommandJson
        }.andExpect {
            status {
                isUnauthorized()
            }
        }
    }

    @Test
    fun `should return 400 Bad Request when invalid body provided`() {
        web.post("/projects") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status {
                isBadRequest()
            }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        val uuid = UUID.randomUUID()
        every { projectModel.createProject(currentUser, createProjectCommand) } returns uuid

        web.post("/projects") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createProjectCommandJson
        }.andExpect {
            status {
                isOk()
                content {
                    string("\"${uuid}\"")
                }
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
