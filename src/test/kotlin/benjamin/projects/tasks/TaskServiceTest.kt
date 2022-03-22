package benjamin.projects.tasks

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
import benjamin.projects.tasks.impl.TaskService
import benjamin.users.api.RegisterUserCommand
import benjamin.users.impl.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TaskService::class, ProjectService::class, UserService::class)
class TaskServiceTest {
    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var taskService: TaskService

    private val project = Project(
        title = "Google",
        description = "Search System"
    )

    private val createUserCommand1 = RegisterUserCommand(
        userName = "adamelmurzaev95",
        firstName = "Adam",
        lastName = "Elmurzaev",
        email = "adamelmurzaev@gmail.com",
        password = "user123"
    )

    private val createUserCommand2 = RegisterUserCommand(
        userName = "ivanandrianov",
        firstName = "Ivan",
        lastName = "Andrianov",
        email = "ivan@gmail.com",
        password = "user123"
    )

    private val createTaskCommand = CreateTaskCommand(
        title = "Google-1",
        description = "Create project",
        projectTitle = "Google",
        assignee = "adamelmurzaev95"
    )

    @BeforeEach
    fun setup() {
        userService.register(createUserCommand1)
        projectService.create(project)
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
        userService.register(createUserCommand2)

        val id1 = (
            taskService.create(
                author = createUserCommand1.userName,
                createCommand = createTaskCommand
            ) as CreateTaskResult.Success
            ).id

        val id2 = (
            taskService.create(
                author = createUserCommand1.userName,
                createCommand = createTaskCommand.copy(title = "Google-2", description = "Create gitlab project")
            ) as CreateTaskResult.Success
            ).id

        taskService.create(
            author = createUserCommand1.userName,
            createCommand = createTaskCommand.copy(
                title = "Google-3",
                description = "Add ci cd",
                assignee = "ivanandrianov"
            )
        )

        taskService.update(id1, UpdateTaskCommand(status = TaskStatus.IN_PROGRESS))
        taskService.update(id2, UpdateTaskCommand(status = TaskStatus.IN_PROGRESS))

        val expected = Tasks(
            tasks = listOf(
                Task("Google-1", createUserCommand1.userName, TaskStatus.IN_PROGRESS),
                Task("Google-2", createUserCommand1.userName, TaskStatus.IN_PROGRESS),
                Task("Google-3", "ivanandrianov", TaskStatus.NEW)
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
        val start = Instant.now()
        val id = (taskService.create(createUserCommand1.userName, createTaskCommand) as CreateTaskResult.Success).id
        taskService.update(id, UpdateTaskCommand(status = TaskStatus.DONE))

        val expected = TaskProfile(
            title = "Google-1",
            description = "Create project",
            projectTitle = "Google",
            author = createUserCommand1.userName,
            assignee = createUserCommand1.userName,
            creationDateTime = start,
            changedDateTime = start,
            status = TaskStatus.DONE
        )

        val actual = taskService.getProfileById(id)

        assertEquals(
            expected,
            actual?.copy(creationDateTime = start, changedDateTime = start)
        )

        val end = Instant.now()

        val creationDateTime = actual!!.creationDateTime.toEpochMilli()
        val changedDateTime = actual.changedDateTime.toEpochMilli()
        assertTrue(
            creationDateTime > start.toEpochMilli() && creationDateTime < end.toEpochMilli()
        )
        assertTrue(
            changedDateTime > start.toEpochMilli() && changedDateTime < end.toEpochMilli()
        )
    }

    @Test
    fun `create should return ProjectNotFound when project with such title doesnt exist`() {
        assertEquals(
            CreateTaskResult.ProjectNotFound,
            taskService.create(
                createUserCommand1.userName,
                createTaskCommand.copy(projectTitle = "Amazon")
            )
        )
    }

    @Test
    fun `create should return AssigneeNotFound when assignee with such userName doesnt exist`() {
        assertEquals(
            CreateTaskResult.AssigneeNotFound,
            taskService.create(
                createUserCommand1.userName,
                createTaskCommand.copy(assignee = "i.andrianov")
            )
        )
    }

    @Test
    fun `create should return Success when assignee is null`() {
        assertTrue(
            taskService.create(
                createUserCommand1.userName,
                createTaskCommand.copy(assignee = null)
            ) is CreateTaskResult.Success
        )
    }

    @Test
    fun `create should return Success when there no problem`() {
        assertTrue(
            taskService.create(
                createUserCommand1.userName,
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
        val id = (taskService.create(createUserCommand1.userName, createTaskCommand) as CreateTaskResult.Success).id
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
        val id = (taskService.create(createUserCommand1.userName, createTaskCommand) as CreateTaskResult.Success).id
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
        val id = (taskService.create(createUserCommand1.userName, createTaskCommand) as CreateTaskResult.Success).id
        assertEquals(
            UpdateTaskResult.Success,
            taskService.update(
                id,
                UpdateTaskCommand(
                    assignee = createUserCommand1.userName,
                    description = "Do something",
                    status = TaskStatus.DONE
                )
            )
        )
    }
}
