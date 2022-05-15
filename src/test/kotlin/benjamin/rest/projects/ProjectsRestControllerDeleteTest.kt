package benjamin.rest.projects

import benjamin.projects.api.DeleteProjectResult
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
import org.springframework.test.web.servlet.delete

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerDeleteTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
    private lateinit var projectModel: ProjectModel

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.delete("/projects/Google") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isUnauthorized()
            }
        }
    }

    @Test
    fun `should return 404 Not Found when project with such title already exists`() {
        Mockito.`when`(projectModel.deleteProject("Google"))
            .thenReturn(DeleteProjectResult.NotFound)

        web.delete("/projects/Google") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        Mockito.`when`(projectModel.deleteProject("Google"))
            .thenReturn(DeleteProjectResult.Success)

        web.delete("/projects/Google") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isOk()
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}
