package benjamin.rest.projects.model

import benjamin.TestContainerPostgres
import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.DeleteProjectResult
import benjamin.projects.api.GetProjectByUuidResult
import benjamin.projects.api.Project
import benjamin.projects.api.Projects
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.DeleteTaskResult
import benjamin.projects.tasks.api.GetTaskProfileByNumber
import benjamin.projects.tasks.api.GetTasksByAssigneeUserAndProjectUuid
import benjamin.projects.tasks.api.GetTasksByProjectUuid
import benjamin.projects.tasks.api.Task
import benjamin.projects.tasks.api.TaskProfile
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.Tasks
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.rest.models.ProjectModel
import benjamin.users.api.User
import benjamin.users.impl.UsersFetcher
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.Instant
import java.util.UUID

@DataJpaTest(properties = [TestContainerPostgres.url])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectModel::class)
class ProjectModelTest {
    @Autowired
    private lateinit var projectModel: ProjectModel

    @MockkBean
    private lateinit var usersFetcher: UsersFetcher

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

    private val updateProjectCommand = UpdateProjectCommand(
        title = "Benjamin"
    )

    private val projectAuthor = "a.elmurzaev95"

    @Test
    fun `getProjectByUuid should return not found when project with such uuid doesnt exist`() {
        assertEquals(
            GetProjectByUuidResult.NotFound,
            projectModel.getProjectByUuid(UUID.randomUUID(), projectAuthor)
        )
    }

    @Test
    fun `getProjectByUuid should return access denied when current user doesnt have access`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            GetProjectByUuidResult.AccessDenied,
            projectModel.getProjectByUuid(projectUuid, "i.andrianov")
        )
    }

    @Test
    fun `getProjectByUuid should return result when current user has access`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        val expected = Project(
            projectUuid,
            createProjectCommand.title,
            createProjectCommand.description,
            projectAuthor
        )

        val actual = projectModel.getProjectByUuid(projectUuid, projectAuthor)

        assert(actual is GetProjectByUuidResult.Success)

        assertEquals(
            expected,
            (actual as GetProjectByUuidResult.Success).project
        )
    }

    @Test
    fun `createProject should create project`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        val expected = Project(
            projectUuid,
            createProjectCommand.title,
            createProjectCommand.description,
            projectAuthor
        )

        val actual = projectModel.getProjectByUuid(projectUuid, projectAuthor)

        assertEquals(
            expected,
            (actual as GetProjectByUuidResult.Success).project
        )
    }

    @Test
    fun `updateProject should return not found if project doesnt exist`() {
        assertEquals(
            UpdateProjectResult.NotFound,
            projectModel.updateProject(UUID.randomUUID(), updateProjectCommand, projectAuthor)
        )
    }

    @Test
    fun `updateProject should return access denied if user doesnt have access`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            UpdateProjectResult.AccessDenied,
            projectModel.updateProject(projectUuid, updateProjectCommand, "islam")
        )
    }

    @Test
    fun `updateProject should update correctly`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            UpdateProjectResult.Success,
            projectModel.updateProject(projectUuid, updateProjectCommand, projectAuthor)
        )

        assertEquals(
            (projectModel.getProjectByUuid(projectUuid, projectAuthor) as GetProjectByUuidResult.Success).project.title,
            "Benjamin"
        )
    }

    @Test
    fun `deleteProject should return not found if project doesnt exist`() {
        assertEquals(DeleteProjectResult.NotFound, projectModel.deleteProject(UUID.randomUUID(), projectAuthor))
    }

    @Test
    fun `deleteProject should return access denied if user doesnt have access`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(DeleteProjectResult.AccessDenied, projectModel.deleteProject(projectUuid, "islam"))
    }

    @Test
    fun `deleteProject should return success and delete project`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(DeleteProjectResult.Success, projectModel.deleteProject(projectUuid, projectAuthor))
        assertEquals(GetProjectByUuidResult.NotFound, projectModel.getProjectByUuid(projectUuid, projectAuthor))
    }

    @Test
    fun `getProjectsByUsername should return correct result`() {
        val projectUuid1 = projectModel.createProject(projectAuthor, createProjectCommand)
        val projectUuid2 = projectModel.createProject(projectAuthor, createProjectCommand.copy(title = "Benjamin"))

        val expected = Projects(
            projects = listOf(
                Project(
                    projectUuid1,
                    createProjectCommand.title,
                    createProjectCommand.description,
                    projectAuthor
                ),
                Project(
                    projectUuid2,
                    "Benjamin",
                    createProjectCommand.description,
                    projectAuthor
                )
            )
        )

        val actual = projectModel.getProjectsByUsername(projectAuthor)

        assertEquals(expected, actual)
    }

    @Test
    fun `getAllTasksByProjectUuid should return ProjectNotFound when no such project exists`() {
        assertEquals(
            GetTasksByProjectUuid.ProjectNotFound,
            projectModel.getAllTasksByProjectUuid(UUID.randomUUID(), "adam")
        )
    }

    @Test
    fun `getAllTasksByProjectUuid should return access denied when used doesnt have access to this project`() {
        val projectUuid1 = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            GetTasksByProjectUuid.AccessDenied,
            projectModel.getAllTasksByProjectUuid(projectUuid1, "adam")
        )
    }

    @Test
    fun `getAllTasksByProjectUuid should return correct result`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskResult1 = projectModel.createTask(projectAuthor, projectUuid, createTaskCommand)
        val taskResult2 =
            projectModel.createTask(projectAuthor, projectUuid, createTaskCommand.copy(title = "Google-2"))

        val expected = Tasks(
            tasks = listOf(
                Task(
                    title = createTaskCommand.title,
                    assignee = createTaskCommand.assignee!!,
                    status = TaskStatus.NEW,
                    number = (taskResult1 as CreateTaskResult.Success).taskNum
                ),
                Task(
                    title = "Google-2",
                    assignee = createTaskCommand.assignee!!,
                    status = TaskStatus.NEW,
                    number = (taskResult2 as CreateTaskResult.Success).taskNum
                )
            )
        )

        val actual = projectModel.getAllTasksByProjectUuid(projectUuid, projectAuthor)

        assertEquals(
            expected,
            (actual as GetTasksByProjectUuid.Success).tasks
        )
    }

    @Test
    fun `getTaskProfileByNumber should return ProjectNotFound when no such project exists`() {
        assertEquals(
            GetTaskProfileByNumber.ProjectNotFound,
            projectModel.getTaskProfileByNumber(1, UUID.randomUUID(), projectAuthor)
        )
    }

    @Test
    fun `getTaskProfileByNumber should return TaskNotFound when no such task exists`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            GetTaskProfileByNumber.TaskNotFound,
            projectModel.getTaskProfileByNumber(1, projectUuid, projectAuthor)
        )
    }

    @Test
    fun `getTaskProfileByNumber should return AccessDenied when user doesnt have access to this project`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskNum =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum
        assertEquals(
            GetTaskProfileByNumber.AccessDenied,
            projectModel.getTaskProfileByNumber(taskNum, projectUuid, "islam")
        )
    }

    @Test
    fun `getTaskProfileByNumber should return correct TaskProfile`() {
        val start = Instant.now()

        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val num =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum

        val expected = TaskProfile(
            title = createTaskCommand.title,
            description = createTaskCommand.description,
            projectTitle = createProjectCommand.title,
            author = projectAuthor,
            assignee = createTaskCommand.assignee!!,
            creationDateTime = start,
            changedDateTime = start,
            status = TaskStatus.NEW,
            number = num
        )

        val end = Instant.now()

        val actual = projectModel.getTaskProfileByNumber(num, projectUuid, projectAuthor)

        assertTrue(actual is GetTaskProfileByNumber.Success)
        val actualTaskProfile = (actual as GetTaskProfileByNumber.Success).taskProfile

        assertEquals(expected, actualTaskProfile.copy(creationDateTime = start, changedDateTime = start))

        val creationDateTime = actualTaskProfile.creationDateTime.toEpochMilli()
        val changedDateTime = actualTaskProfile.changedDateTime.toEpochMilli()

        assertTrue(creationDateTime > start.toEpochMilli())
        assertTrue(creationDateTime < end.toEpochMilli())
        assertTrue(changedDateTime > start.toEpochMilli())
        assertTrue(changedDateTime < end.toEpochMilli())
    }

    @Test
    fun `getTasksByAssigneeAndProjectUuid should return ProjectNotFound when no such project exists`() {
        assertEquals(
            GetTasksByAssigneeUserAndProjectUuid.ProjectNotFound,
            projectModel.getTasksByAssigneeAndProjectUuid(projectAuthor, UUID.randomUUID())
        )
    }

    @Test
    fun `getTasksByAssigneeAndProjectUuid should return access denied when user doesnt have access to this project`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        assertEquals(
            GetTasksByAssigneeUserAndProjectUuid.AccessDenied,
            projectModel.getTasksByAssigneeAndProjectUuid("islam", projectUuid)
        )
    }

    @Test
    fun `getTasksByAssigneeAndProjectUuid should return correct result`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val projectUuid2 = projectModel.createProject(projectAuthor, createProjectCommand.copy(title = "HRMS"))
        val taskResult1 = projectModel.createTask(projectAuthor, projectUuid, createTaskCommand)
        val taskResult2 =
            projectModel.createTask(projectAuthor, projectUuid, createTaskCommand.copy(title = "Google-2"))
        val taskResult3 = projectModel.createTask(projectAuthor, projectUuid2, createTaskCommand.copy(title = "HRMS-1"))

        val expected1 = Tasks(
            tasks = listOf(
                Task(
                    title = createTaskCommand.title,
                    assignee = createTaskCommand.assignee!!,
                    status = TaskStatus.NEW,
                    number = (taskResult1 as CreateTaskResult.Success).taskNum
                ),
                Task(
                    title = "Google-2",
                    assignee = createTaskCommand.assignee!!,
                    status = TaskStatus.NEW,
                    number = (taskResult2 as CreateTaskResult.Success).taskNum
                )
            )
        )

        val actual1 = projectModel.getTasksByAssigneeAndProjectUuid(projectAuthor, projectUuid)

        assertEquals(
            expected1,
            (actual1 as GetTasksByAssigneeUserAndProjectUuid.Success).tasks
        )

        val expected2 = Tasks(
            tasks = listOf(
                Task(
                    title = "HRMS-1",
                    assignee = createTaskCommand.assignee!!,
                    status = TaskStatus.NEW,
                    number = (taskResult3 as CreateTaskResult.Success).taskNum
                )
            )
        )

        val actual2 = projectModel.getTasksByAssigneeAndProjectUuid(projectAuthor, projectUuid2)

        assertEquals(expected2, (actual2 as GetTasksByAssigneeUserAndProjectUuid.Success).tasks)
    }

    @Test
    fun `createTask should return ProjectNotFound when no such project exists`() {
        assertEquals(
            CreateTaskResult.ProjectNotFound,
            projectModel.createTask(projectAuthor, UUID.randomUUID(), createTaskCommand)
        )
    }

    @Test
    fun `createTask should return AccessDenied when user doesnt have access to this project`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            CreateTaskResult.AccessDenied,
            projectModel.createTask("islam95", projectUuid, createTaskCommand)
        )
    }

    @Test
    fun `createTask should return AssigneeNotFound when assignee with such username doesnt exist`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        every { usersFetcher.fetchByUserName("islam95") } returns emptyList()

        assertEquals(
            CreateTaskResult.AssigneeNotFound,
            projectModel.createTask(projectAuthor, projectUuid, createTaskCommand.copy(assignee = "islam95"))
        )
    }

    @Test
    fun `createTask should return AssigneeHasNoAccess when assignee doesnt has access to this project`() {
        val user = User(
            "islam95",
            "Islam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName("islam95") } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            CreateTaskResult.AssigneeHasNoAccess,
            projectModel.createTask(projectAuthor, projectUuid, createTaskCommand.copy(assignee = "islam95"))
        )
    }

    @Test
    fun `createTask should work correctly`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val result = projectModel.createTask(projectAuthor, projectUuid, createTaskCommand)

        assertTrue(result is CreateTaskResult.Success)
    }

    @Test
    fun `updateTask should return ProjectNotFound when no such project exists`() {
        assertEquals(
            UpdateTaskResult.ProjectNotFound,
            projectModel.updateTask(1, UUID.randomUUID(), projectAuthor, updateTaskCommand)
        )
    }

    @Test
    fun `updateTask should return TaskNotFound when no such task exists`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(
            UpdateTaskResult.TaskNotFound,
            projectModel.updateTask(1, projectUuid, projectAuthor, updateTaskCommand)
        )
    }

    @Test
    fun `updateTask should return AccessDenied when user doesnt have access to this project`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskNum =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum

        assertEquals(
            UpdateTaskResult.AccessDenied,
            projectModel.updateTask(taskNum, projectUuid, "islam95", updateTaskCommand)
        )
    }

    @Test
    fun `updateTask should return AssigneeNotFound when no such assignee with this username`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskNum =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum

        every { usersFetcher.fetchByUserName("islam95") } returns emptyList()

        assertEquals(
            UpdateTaskResult.AssigneeNotFound,
            projectModel.updateTask(taskNum, projectUuid, projectAuthor, updateTaskCommand.copy(assignee = "islam95"))
        )
    }

    @Test
    fun `updateTask should return AssigneeHasNoAccess when assignee doesnt have access to this project`() {
        val user1 = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user1)

        val user2 = User(
            "islam95",
            "Islam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName("islam95") } returns listOf(user2)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskNum =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum

        assertEquals(
            UpdateTaskResult.AssigneeHasNoAccess,
            projectModel.updateTask(taskNum, projectUuid, projectAuthor, updateTaskCommand.copy(assignee = "islam95"))
        )
    }

    @Test
    fun `updateTask should work correctly`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskNum =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum

        assertEquals(
            UpdateTaskResult.Success,
            projectModel.updateTask(taskNum, projectUuid, projectAuthor, updateTaskCommand)
        )
    }

    @Test
    fun `deleteTask should return ProjectNotFound when no such project exist`() {
        assertEquals(
            DeleteTaskResult.ProjectNotFound,
            projectModel.deleteTask(1, UUID.randomUUID(), projectAuthor)
        )
    }

    @Test
    fun `deleteTask should return TaskNotFound when no such task exist`() {
        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)

        assertEquals(DeleteTaskResult.TaskNotFound, projectModel.deleteTask(1, projectUuid, projectAuthor))
    }

    @Test
    fun `deleteTask should return AccessDenied when user doesnt have access to this project`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskNum =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum

        assertEquals(DeleteTaskResult.AccessDenied, projectModel.deleteTask(taskNum, projectUuid, "islam"))
    }

    @Test
    fun `deleteTask should work correctly`() {
        val user = User(
            createTaskCommand.assignee!!,
            "Adam",
            "Elmurzaev"
        )

        every { usersFetcher.fetchByUserName(createTaskCommand.assignee!!) } returns listOf(user)

        val projectUuid = projectModel.createProject(projectAuthor, createProjectCommand)
        val taskNum =
            (projectModel.createTask(projectAuthor, projectUuid, createTaskCommand) as CreateTaskResult.Success).taskNum

        assertEquals(
            DeleteTaskResult.Success,
            projectModel.deleteTask(taskNum, projectUuid, projectAuthor)
        )
    }
}
