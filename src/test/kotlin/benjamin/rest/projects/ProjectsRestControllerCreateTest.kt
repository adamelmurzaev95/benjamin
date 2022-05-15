package benjamin.rest.projects

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.CreateProjectResult
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

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerCreateTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
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
    fun `should return 409 Conflict when project with such title already exists`() {
        Mockito.`when`(projectModel.createProject("adamelmurzaev95", createProjectCommand))
            .thenReturn(CreateProjectResult.AlreadyExists)

        web.post("/projects") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createProjectCommandJson
        }.andExpect {
            status {
                isConflict()
            }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        Mockito.`when`(projectModel.createProject("adamelmurzaev95", createProjectCommand))
            .thenReturn(CreateProjectResult.Success)

        web.post("/projects") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = createProjectCommandJson
        }.andExpect {
            status {
                isOk()
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}
