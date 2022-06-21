package benjamin.projects.tasks.impl

import benjamin.TestContainerPostgres
import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.impl.ProjectRepository
import benjamin.projects.impl.ProjectService
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.Task
import benjamin.projects.tasks.api.TaskProfile
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.Tasks
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Instant
import java.util.UUID

@DataJpaTest(properties = [TestContainerPostgres.url])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskServiceTest {
    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    private lateinit var projectService: ProjectService

    private lateinit var taskService: TaskService

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

    lateinit var uuid: UUID

    @BeforeEach
    fun setup() {
        taskService = TaskService(taskRepository, projectRepository)
        projectService = ProjectService(projectRepository)
        uuid = projectService.create(username1, createProjectCommand)
    }

    @Test
    fun `getAllByProjectUuid should return empty list if no tasks by this uuid`() {
        assertEquals(
            Tasks(emptyList()),
            taskService.getAllByProjectUuid(uuid)
        )
    }

    @Test
    fun `getAllByProjectUuid should return correct response`() {
        val num1 = taskService.create(username1, uuid, createTaskCommand)
        val num2 = taskService.create(
            username2,
            uuid,
            createTaskCommand.copy(title = "Google-2", description = "Create repo")
        )

        val expected = Tasks(
            tasks = listOf(
                Task(
                    title = "Google-1",
                    assignee = username1,
                    status = TaskStatus.NEW,
                    number = num1
                ),
                Task(
                    title = "Google-2",
                    assignee = username1,
                    status = TaskStatus.NEW,
                    number = num2
                )
            )
        )

        val actual = taskService.getAllByProjectUuid(uuid)

        assertEquals(expected, actual)
    }

    @Test
    fun `getAllByAssigneeAndProjectUuid should return empty list`() {
        assertTrue(taskService.getAllByAssigneeAndProjectUuid(username1, uuid).tasks.isEmpty())
    }

    @Test
    fun `getAllByAssigneeAndProjectUuid should return correct list`() {
        val num1 = taskService.create(username1, uuid, createTaskCommand)
        val num2 = taskService.create(username1, uuid, createTaskCommand.copy(title = "Google-2"))
        taskService.create(username1, uuid, createTaskCommand.copy(title = "Google-3", assignee = "islam"))

        val expected = Tasks(
            tasks = listOf(
                Task(
                    createTaskCommand.title,
                    createTaskCommand.assignee!!,
                    TaskStatus.NEW,
                    num1
                ),
                Task(
                    "Google-2",
                    createTaskCommand.assignee!!,
                    TaskStatus.NEW,
                    num2
                )
            )
        )

        val actual = taskService.getAllByAssigneeAndProjectUuid(username1, uuid)

        assertEquals(expected, actual)
    }

    @Test
    fun `getProfileByNumberAndProjectUuid should return correct result`() {
        val start = Instant.now()
        val num = taskService.create(username1, uuid, createTaskCommand)

        val expected = TaskProfile(
            title = "Google-1",
            description = "Create project",
            projectTitle = "Google",
            author = username1,
            assignee = username1,
            creationDateTime = start,
            changedDateTime = start,
            status = TaskStatus.NEW,
            number = num
        )

        val actual = taskService.getProfileByNumberAndProjectUuid(num, uuid)

        assertEquals(
            expected,
            actual.copy(creationDateTime = start, changedDateTime = start)
        )

        val end = Instant.now()

        val creationDateTime = actual.creationDateTime.toEpochMilli()
        val changedDateTime = actual.changedDateTime.toEpochMilli()

        assertTrue(creationDateTime > start.toEpochMilli())
        assertTrue(creationDateTime < end.toEpochMilli())
        assertTrue(changedDateTime > start.toEpochMilli())
        assertTrue(changedDateTime < end.toEpochMilli())
    }

    @Test
    fun `update should update task`() {
        val num = taskService.create(username1, uuid, createTaskCommand)

        val result = taskService.update(
            number = num,
            projectUuid = uuid,
            updateCommand = UpdateTaskCommand(
                assignee = username2,
                description = "Do something",
                status = TaskStatus.DONE
            )
        )
        assertEquals(
            UpdateTaskResult.Success,
            result
        )

        val expected = Task(
            title = createTaskCommand.title,
            assignee = username2,
            status = TaskStatus.DONE,
            number = num
        )

        assertEquals(
            expected,
            taskService.getAllByProjectUuid(uuid).tasks.first { it.number == num }
        )
    }

    @Test
    fun `delete should delete task`() {
        val num = taskService.create(username1, uuid, createTaskCommand)

        taskService.delete(num, uuid)

        assertFalse(taskService.existsByProjectUuidAndNumber(num, uuid))
    }

    @Test
    fun `existByProjectUuidAndNumber should return true when task exists`() {
        val num = taskService.create(username1, uuid, createTaskCommand)

        assertTrue(taskService.existsByProjectUuidAndNumber(num, uuid))
    }
}
