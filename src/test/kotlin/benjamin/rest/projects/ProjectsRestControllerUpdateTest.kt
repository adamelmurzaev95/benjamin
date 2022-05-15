package benjamin.rest.projects

import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
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
import org.springframework.test.web.servlet.put

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerUpdateTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
    private lateinit var projectModel: ProjectModel

    private val updateProjectCommand = UpdateProjectCommand(
        description = "Projects management system"
    )

    private val updateProjectCommandJson = """
        {
          "description": "Projects management system"
        }
    """.trimIndent()

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.put("/projects/Google") {
            contentType = MediaType.APPLICATION_JSON
            content = updateProjectCommandJson
        }.andExpect {
            status {
                isUnauthorized()
            }
        }
    }

    @Test
    fun `should return 400 Bad Request when invalid body provided`() {
        web.put("/projects/Google") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = "{"
        }.andExpect {
            status {
                isBadRequest()
            }
        }
    }

    @Test
    fun `should return 404 Not Found when project with such title doesnt exist`() {
        Mockito.`when`(projectModel.updateProject("Google", updateProjectCommand))
            .thenReturn(UpdateProjectResult.NotFound)

        web.put("/projects/Google") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateProjectCommandJson
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        Mockito.`when`(projectModel.updateProject("Google", updateProjectCommand))
            .thenReturn(UpdateProjectResult.Success)

        web.put("/projects/Google") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateProjectCommandJson
        }.andExpect {
            status {
                isOk()
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}
