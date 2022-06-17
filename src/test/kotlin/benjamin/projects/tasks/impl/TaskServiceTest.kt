package benjamin.projects.tasks.impl

import benjamin.TestContainerPostgres
import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.Project
import benjamin.projects.impl.ProjectService
import benjamin.projects.tasks.api.CreateTaskCommand
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

@DataJpaTest(properties = [TestContainerPostgres.url])
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
        assignee = "adamelmurzaev95"
    )

    private val username1 = "adamelmurzaev95"

    private val username2 = "ivanandrianov"

    private val projectTitle = "Google"

    @BeforeEach
    fun setup() {
        projectService.create(project.author, createProjectCommand)
    }

    @Test
    fun `getAllByProjectTitle should return empty map if no project with such title`() {
        assertEquals(
            Tasks(emptyList()),
            taskService.getAllByProjectTitle("Google")
        )
    }

    @Test
    fun `getAllByProjectTitle should return correct response`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)

        Mockito.`when`(userService.existsByUserName(username2))
            .thenReturn(true)

        val id1 = taskService.create(username1, projectTitle, createTaskCommand)

        val id2 = taskService.create(
            author = username1,
            projectTitle = projectTitle,
            createCommand = createTaskCommand.copy(title = "Google-2", description = "Create gitlab project")
        )

        val id3 = taskService.create(
            author = username2,
            projectTitle = projectTitle,
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
                Task(id1, "Google-1", username1, TaskStatus.IN_PROGRESS),
                Task(id2, "Google-2", username1, TaskStatus.IN_PROGRESS),
                Task(id3, "Google-3", username2, TaskStatus.NEW)
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
        val id = taskService.create(username1, projectTitle, createTaskCommand)

        val expected = TaskProfile(
            id = id,
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
    fun `update should return Success when there no problem`() {
        Mockito.`when`(userService.existsByUserName(username1))
            .thenReturn(true)

        val id = taskService.create(username1, projectTitle, createTaskCommand)
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
