package benjamin.rest.projects

import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put
import java.util.UUID

@WebMvcTest(controllers = [TasksRestController::class])
@Import(Oauth2SecurityConfig::class)
class TasksRestControllerUpdateTest {
    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var projectModel: ProjectModel

    private val updateTaskCommandJson = """
        {
          "description": "Update dependencies",
          "assignee": "adamelmurzaev95",
          "status": "DONE"
        }
    """.trimIndent()

    private val updateTaskCommand = UpdateTaskCommand(
        description = "Update dependencies",
        assignee = "adamelmurzaev95",
        status = TaskStatus.DONE
    )

    private val uuid = UUID.randomUUID()

    private val currentUser = "adamelmurzaev95"

    @Test
    fun `should return 401 Unauthorized when no jwt token is provided`() {
        web.put("/projects/$uuid/tasks/1")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }
    }

    @Test
    fun `should return 400 Bad Request when invalid body provided`() {
        web.put("/projects/$uuid/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = "{"
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 404 Not Found when assignee with such username doesnt exist`() {
        every { projectModel.updateTask(1, uuid, currentUser, updateTaskCommand) } returns UpdateTaskResult.AssigneeNotFound

        web.put("/projects/$uuid/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 404 Not Found when task with such number doesnt exist`() {
        every { projectModel.updateTask(1, uuid, currentUser, updateTaskCommand) } returns UpdateTaskResult.TaskNotFound

        web.put("/projects/$uuid/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 404 Not Found when project with such uuid doesnt exist`() {
        every { projectModel.updateTask(1, uuid, currentUser, updateTaskCommand) } throws ProjectNotFoundException("Project not found")

        web.put("/projects/$uuid/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 403 Forbidden when user doesnt have access to this project`() {
        every { projectModel.updateTask(1, uuid, currentUser, updateTaskCommand) } throws ProjectAccessDeniedException("Access denied")

        web.put("/projects/$uuid/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `should return 409 Conflict when assignee doesnt have access to this project`() {
        every { projectModel.updateTask(1, uuid, currentUser, updateTaskCommand) } returns UpdateTaskResult.AssigneeHasNoAccess

        web.put("/projects/$uuid/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status {
                isConflict()
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json("{\"error\": \"Assignee hasn't access to this project\"}")
                }
            }
        }
    }

    @Test
    fun `should return 200 OK when there no problems`() {
        every { projectModel.updateTask(1, uuid, currentUser, updateTaskCommand) } returns UpdateTaskResult.Success

        web.put("/projects/$uuid/tasks/1") {
            mockJwt()
            contentType = MediaType.APPLICATION_JSON
            content = updateTaskCommandJson
        }.andExpect {
            status { isOk() }
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt() =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", currentUser) })
}
