package benjamin.rest.projects

import benjamin.projects.tasks.api.DeleteTaskResult
import benjamin.rest.projects.models.ProjectModel
import benjamin.security.Oauth2SecurityConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import java.util.UUID

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerDeleteTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val currentUser = "adamelmurzaev95"

    private val projectUuid = UUID.randomUUID()

    @Test
    fun `should return 401 Unauthorized when no jwt token provided`() {
        web.delete("/projects/$projectUuid/tasks/1")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 404 NotFound when no such project exists`() {
        every { projectModel.deleteTask(1, projectUuid, currentUser) } returns DeleteTaskResult.ProjectNotFound

        web.delete("/projects/$projectUuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 404 NotFound when no such task exists`() {
        every { projectModel.deleteTask(1, projectUuid, currentUser) } returns DeleteTaskResult.TaskNotFound

        web.delete("/projects/$projectUuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isNotFound()
            }
        }
    }

    @Test
    fun `should return 403 Forbidden when user doesnt have access to this project`() {
        every { projectModel.deleteTask(1, projectUuid, currentUser) } returns DeleteTaskResult.AccessDenied

        web.delete("/projects/$projectUuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isForbidden()
            }
        }
    }

    @Test
    fun `should return 200 OK`() {
        every { projectModel.deleteTask(1, projectUuid, currentUser) } returns DeleteTaskResult.Success

        web.delete("/projects/$projectUuid/tasks/1") {
            mockJwt()
        }.andExpect {
            status {
                isOk()
            }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
