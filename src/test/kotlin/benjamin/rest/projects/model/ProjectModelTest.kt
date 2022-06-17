package benjamin.rest.projects.model

import benjamin.TestContainerPostgres
import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.impl.ProjectService
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.projects.tasks.impl.TaskService
import benjamin.rest.models.ProjectModel
import benjamin.users.impl.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import

@DataJpaTest(properties = [TestContainerPostgres.url])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectModel::class, TaskService::class, ProjectService::class)
class ProjectModelTest {
    @Autowired
    private lateinit var projectModel: ProjectModel

    @Autowired
    private lateinit var taskService: TaskService

    @MockBean
    private lateinit var userService: UserService

    private val createProjectCommand = CreateProjectCommand(
        title = "Google",
        description = "Search System"
    )

    private val createTaskCommand = CreateTaskCommand(
        title = "Google-1",
        description = "Create project",
        assignee = "a.elmurzaev95"
    )

    private val updateTaskCommand = UpdateTaskCommand(
        assignee = "a.elmurzaev95",
        description = "Update dependencies",
        status = TaskStatus.DONE
    )

    @BeforeEach
    fun setup() {
        projectModel.createProject("adamelmurzaev95", createProjectCommand)
    }

    @Test
    fun `createTask should return ProjectNotFound when project with such title doesnt exist`() {
        assertEquals(
            CreateTaskResult.ProjectNotFound,
            projectModel.createTask("adamelmurzaev95", "Facebook", createTaskCommand)
        )
    }

    @Test
    fun `createTask should return AssigneeNotFound when assignee with such username doesnt exist`() {
        Mockito.`when`(userService.existsByUserName("a.elmurzaev95"))
            .thenReturn(false)

        assertEquals(
            CreateTaskResult.AssigneeNotFound,
            projectModel.createTask("adamelmurzaev95", "Google", createTaskCommand)
        )
    }

    @Test
    fun `createTask should return Success when assignee is null`() {
        assertEquals(
            CreateTaskResult.Success,
            projectModel.createTask(
                "adamelmurzaev95", "Google", createTaskCommand.copy(assignee = null)
            )
        )
    }

    @Test
    fun `createTask should return Success when there no problem`() {
        Mockito.`when`(userService.existsByUserName("a.elmurzaev95"))
            .thenReturn(true)

        assertEquals(
            CreateTaskResult.Success,
            projectModel.createTask(
                "adamelmurzaev95", "Google", createTaskCommand
            )
        )
    }

    @Test
    fun `updateTask should return AssigneeNotFound when assignee with such username doesnt exist`() {
        val id = taskService.create("adamelmurzaev95", "Google", createTaskCommand)

        Mockito.`when`(userService.existsByUserName("a.elmurzaev95"))
            .thenReturn(false)

        assertEquals(
            UpdateTaskResult.AssigneeNotFound,
            projectModel.updateTask(id, updateTaskCommand)
        )
    }

    @Test
    fun `updateTask should return Success when assignee is null`() {
        val id = taskService.create("adamelmurzaev95", "Google", createTaskCommand)

        assertEquals(
            UpdateTaskResult.Success,
            projectModel.updateTask(id, updateTaskCommand.copy(assignee = null))
        )
    }

    @Test
    fun `updateTask should return Success when there no problem`() {
        val id = taskService.create("adamelmurzaev95", "Google", createTaskCommand)

        Mockito.`when`(userService.existsByUserName("a.elmurzaev95"))
            .thenReturn(true)

        assertEquals(
            UpdateTaskResult.Success,
            projectModel.updateTask(id, updateTaskCommand)
        )
    }
}
