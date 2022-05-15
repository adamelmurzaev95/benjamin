package benjamin.rest.projects

import benjamin.projects.api.Project
import benjamin.rest.models.ProjectModel
import benjamin.security.Oauth2SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [ProjectsRestController::class])
@Import(Oauth2SecurityConfig::class)
class ProjectsRestControllerGetByTitleTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockBean
    private lateinit var projectModel: ProjectModel

    private val projectJson = """
        {
          "title": "Google",
          "description": "American multinational technology company",
          "author": "adamelmurzaev95"
        }
    """.trimIndent()

    private val project = Project(
        title = "Google",
        description = "American multinational technology company",
        author = "adamelmurzaev95"
    )

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.get("/projects/Google")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 404 Not Found when no project with such title`() {
        web.get("/projects/Google") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 200 OK with correct body`() {
        Mockito.`when`(projectModel.getProjectByTitle("Google"))
            .thenReturn(project)

        web.get("/projects/Google") {
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
        with(jwt().jwt { it.claim("user_name", "adamelmurzaev95") })
}
