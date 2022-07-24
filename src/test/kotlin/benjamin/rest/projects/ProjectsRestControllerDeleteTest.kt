package benjamin.rest.projects

import benjamin.projects.api.DeleteProjectResult
import benjamin.rest.projects.models.ProjectModel
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
import java.util.UUID

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerDeleteTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
    private lateinit var projectModel: ProjectModel

    private val uuid = UUID.randomUUID()

    private val currentUser = "adamelmurzaev95"

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
    fun `should return 404 Not Found when project with such uuid doesnt exist`() {
        Mockito.`when`(projectModel.deleteProject(uuid, currentUser))
            .thenReturn(DeleteProjectResult.NotFound)

        web.delete("/projects/$uuid") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 403 Forbidden when user doesnt have access to this project`() {
        Mockito.`when`(projectModel.deleteProject(uuid, currentUser))
            .thenReturn(DeleteProjectResult.AccessDenied)

        web.delete("/projects/$uuid") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isForbidden()
            }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        Mockito.`when`(projectModel.deleteProject(uuid, currentUser))
            .thenReturn(DeleteProjectResult.Success)

        web.delete("/projects/$uuid") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isOk()
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
