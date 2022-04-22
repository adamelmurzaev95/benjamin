package benjamin.projects.tasks.impl

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.Project
import benjamin.projects.impl.ProjectService
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.Task
import benjamin.projects.tasks.api.TaskProfile
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.Tasks
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.users.impl.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TaskService::class, ProjectService::class)
class TaskServiceTest {
    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired
    private lateinit var taskService: TaskService

    @MockBean
    private lateinit var userService: UserService

    private val project = Project(
        title = "Google",
        description = "Search System",
        author = "adamelmurzaev95"
    )

    private val createProjectCommand = CreateProjectCommand(
        title = "Google",
        description = "Search System"
    )

    private val createTaskCommand = CreateTaskCommand(
        title = "Google-1",
        description = "Create project",
        projectTitle = "Google",
        assignee = "adamelmurzaev95"
    )

    private val username1 = "adamelmurzaev95"

    private val username2 = "ivanandrianov"

    @BeforeEach
    fun setup() {
        projectService.create(project.author, createProjectCommand)
    }

    @Test
    fun `getByProjectTitle should return empty map if no project with such title`() {
        assertEquals(
            Tasks(emptyList()),
            taskService.getAllByProjectTitle("Google")
        )
    }

    @Test
    fun `getByProjectTitle should return correct response`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)

        Mockito.`when`(userService.existsByUserName(username2))
            .thenReturn(true)

        val id1 = (
            taskService.create(
                author = username1,
                createCommand = createTaskCommand
            ) as CreateTaskResult.Success
            ).id

        val id2 = (
            taskService.create(
                author = username1,
                createCommand = createTaskCommand.copy(title = "Google-2", description = "Create gitlab project")
            ) as CreateTaskResult.Success
            ).id

        taskService.create(
            author = username2,
            createCommand = createTaskCommand.copy(
                title = "Google-3",
                description = "Add ci cd",
                assignee = username2
            )
        )

        taskService.update(id1, UpdateTaskCommand(status = TaskStatus.IN_PROGRESS))
        taskService.update(id2, UpdateTaskCommand(status = TaskStatus.IN_PROGRESS))

        val expected = Tasks(
            tasks = listOf(
                Task("Google-1", username1, TaskStatus.IN_PROGRESS),
                Task("Google-2", username1, TaskStatus.IN_PROGRESS),
                Task("Google-3", username2, TaskStatus.NEW)
            )
        )

        val actual = taskService.getAllByProjectTitle("Google")

        assertEquals(
            expected,
            actual
        )
    }

    @Test
    fun `getProfileByTitle should return null if no task with such title`() {
        assertNull(
            taskService.getProfileById(1)
        )
    }

    @Test
    fun `getProfileByTitle should return correct result`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)

        val start = Instant.now()
        val id = (taskService.create(username1, createTaskCommand) as CreateTaskResult.Success).id

        val expected = TaskProfile(
            title = "Google-1",
            description = "Create project",
            projectTitle = "Google",
            author = username1,
            assignee = username1,
            creationDateTime = start,
            changedDateTime = start,
            status = TaskStatus.NEW
        )

        val actual = taskService.getProfileById(id)

        assertEquals(
            expected,
            actual?.copy(creationDateTime = start, changedDateTime = start)
        )

        val end = Instant.now()

        val creationDateTime = actual!!.creationDateTime.toEpochMilli()
        val changedDateTime = actual.changedDateTime.toEpochMilli()

        assertTrue(creationDateTime > start.toEpochMilli())
        assertTrue(creationDateTime < end.toEpochMilli())
        assertTrue(changedDateTime > start.toEpochMilli())
        assertTrue(changedDateTime < end.toEpochMilli())
    }

    @Test
    fun `create should return ProjectNotFound when project with such title doesnt exist`() {
        assertEquals(
            CreateTaskResult.ProjectNotFound,
            taskService.create(
                username1,
                createTaskCommand.copy(projectTitle = "Amazon")
            )
        )
    }

    @Test
    fun `create should return AssigneeNotFound when assignee with such userName doesnt exist`() {
        assertEquals(
            CreateTaskResult.AssigneeNotFound,
            taskService.create(
                username1,
                createTaskCommand.copy(assignee = "i.andrianov")
            )
        )
    }

    @Test
    fun `create should return Success when assignee is null`() {
        assertTrue(
            taskService.create(
                username1,
                createTaskCommand.copy(assignee = null)
            ) is CreateTaskResult.Success
        )
    }

    @Test
    fun `create should return Success when there no problem`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)

        assertTrue(
            taskService.create(
                username1,
                createTaskCommand
            ) is CreateTaskResult.Success
        )
    }

    @Test
    fun `update should return TaskNotFound when task with such id doesnt exist`() {
        assertEquals(
            UpdateTaskResult.TaskNotFound,
            taskService.update(
                1,
                updateCommand = UpdateTaskCommand()
            )
        )
    }

    @Test
    fun `update should return AssigneeNotFound when assignee with such userName doesnt exist`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)
            .thenReturn(false)

        val id = (taskService.create(username1, createTaskCommand) as CreateTaskResult.Success).id
        assertEquals(
            UpdateTaskResult.AssigneeNotFound,
            taskService.update(
                id,
                updateCommand = UpdateTaskCommand(assignee = "adam")
            )
        )
    }

    @Test
    fun `update should return Success when assignee is null`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)

        val id = (taskService.create(username1, createTaskCommand) as CreateTaskResult.Success).id
        assertEquals(
            UpdateTaskResult.Success,
            taskService.update(
                id,
                UpdateTaskCommand(assignee = null, description = "Do something", status = TaskStatus.DONE)
            )
        )
    }

    @Test
    fun `update should return Success when there no problem`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)

        val id = (taskService.create(username1, createTaskCommand) as CreateTaskResult.Success).id
        assertEquals(
            UpdateTaskResult.Success,
            taskService.update(
                id,
                UpdateTaskCommand(
                    assignee = username1,
                    description = "Do something",
                    status = TaskStatus.DONE
                )
            )
        )
    }
}
