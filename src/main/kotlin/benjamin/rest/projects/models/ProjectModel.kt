package benjamin.rest.projects.models

import benjamin.projects.api.ChangeUserRoleCommand
import benjamin.projects.api.ChangeUserRoleResult
import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.Projects
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.impl.ProjectAuthority
import benjamin.projects.impl.ProjectRepository
import benjamin.projects.impl.ProjectService
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.DeleteTaskResult
import benjamin.projects.tasks.api.GetTaskProfileByNumber
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.projects.tasks.impl.TaskRepository
import benjamin.projects.tasks.impl.TaskService
import benjamin.security.ProjectChecks
import benjamin.users.impl.UserService
import benjamin.users.impl.UsersFetcher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class ProjectModel(
    projectRepository: ProjectRepository,
    taskRepository: TaskRepository,
    usersFetcher: UsersFetcher
) {
    private val projectService = ProjectService(projectRepository)
    private val taskService = TaskService(taskRepository, projectRepository)
    private val userService = UserService(usersFetcher)

    @Transactional(readOnly = true)
    @ProjectChecks(projectUuidPath = "uuid")
    fun getProjectByUuid(
        uuid: UUID,
        currentUsername: String
    ) = projectService.getByUuid(uuid)

    @Transactional
    fun createProject(
        author: String,
        createProjectCommand: CreateProjectCommand
    ) = projectService.create(author, createProjectCommand)

    @Transactional
    @ProjectChecks(projectUuidPath = "uuid", requiredAuthority = ProjectAuthority.UPDATE_PROJECT)
    fun updateProject(
        uuid: UUID,
        updateProjectCommand: UpdateProjectCommand,
        currentUsername: String
    ) = projectService.update(uuid, updateProjectCommand)

    @Transactional
    @ProjectChecks(projectUuidPath = "uuid", requiredAuthority = ProjectAuthority.DELETE_PROJECT)
    fun deleteProject(uuid: UUID, currentUsername: String) {
        taskService.deleteAllByProjectUuid(uuid)
        projectService.delete(uuid)
    }

    @Transactional(readOnly = true)
    fun getProjectsByUsername(username: String): Projects {
        return Projects(projectService.getProjectsByUsername(username))
    }

    @Transactional(readOnly = true)
    @ProjectChecks(projectUuidPath = "projectUuid")
    fun getAllTasksByProjectUuid(projectUuid: UUID, currentUsername: String) =
        taskService.getAllByProjectUuid(projectUuid)

    @Transactional
    @ProjectChecks(projectUuidPath = "projectUuid", requiredAuthority = ProjectAuthority.ASSIGN_ROLES)
    fun changeRole(
        projectUuid: UUID,
        currentUsername: String,
        changeUserRoleCommand: ChangeUserRoleCommand
    ): ChangeUserRoleResult {
        val targetUser = changeUserRoleCommand.userName
        return withUserValidation(
            targetUser,
            projectUuid,
            ChangeUserRoleResult.TargetUserNotFound,
            ChangeUserRoleResult.TargetUserHasNoAccess
        ) {
            projectService.changeRole(projectUuid, targetUser, changeUserRoleCommand.role)
            ChangeUserRoleResult.Success
        }
    }

    @Transactional(readOnly = true)
    @ProjectChecks(projectUuidPath = "projectUuid")
    fun getTaskProfileByNumber(number: Int, projectUuid: UUID, currentUsername: String): GetTaskProfileByNumber {
        if (!taskService.existsByProjectUuidAndNumber(number, projectUuid)) {
            return GetTaskProfileByNumber.TaskNotFound
        }
        return GetTaskProfileByNumber.Success(taskService.getProfileByNumberAndProjectUuid(number, projectUuid))
    }

    @Transactional
    @ProjectChecks(projectUuidPath = "projectUuid")
    fun getTasksByAssigneeAndProjectUuid(
        assignee: String,
        projectUuid: UUID
    ) = taskService.getAllByAssigneeAndProjectUuid(assignee, projectUuid)

    @Transactional
    @ProjectChecks(projectUuidPath = "projectUuid", requiredAuthority = ProjectAuthority.CREATE_TASK)
    fun createTask(author: String, projectUuid: UUID, createTaskCommand: CreateTaskCommand): CreateTaskResult {
        val assignee = createTaskCommand.assignee
        if (assignee != null) {
            return withUserValidation(
                assignee,
                projectUuid,
                CreateTaskResult.AssigneeNotFound,
                CreateTaskResult.AssigneeHasNoAccess
            ) { CreateTaskResult.Success(taskService.create(author, projectUuid, createTaskCommand)) }
        }
        val taskNum = taskService.create(author, projectUuid, createTaskCommand)
        return CreateTaskResult.Success(taskNum)
    }

    @Transactional
    @ProjectChecks(projectUuidPath = "projectUuid", requiredAuthority = ProjectAuthority.UPDATE_TASK)
    fun updateTask(
        number: Int,
        projectUuid: UUID,
        currentUsername: String,
        updateTaskCommand: UpdateTaskCommand
    ): UpdateTaskResult {
        if (!taskService.existsByProjectUuidAndNumber(number, projectUuid)) return UpdateTaskResult.TaskNotFound

        val assignee = updateTaskCommand.assignee
        if (assignee != null) {
            return withUserValidation(
                assignee,
                projectUuid,
                UpdateTaskResult.AssigneeNotFound,
                UpdateTaskResult.AssigneeHasNoAccess
            ) {
                taskService.update(number, projectUuid, updateTaskCommand)
                UpdateTaskResult.Success
            }
        }
        taskService.update(number, projectUuid, updateTaskCommand)
        return UpdateTaskResult.Success
    }

    @Transactional
    @ProjectChecks(projectUuidPath = "projectUuid", requiredAuthority = ProjectAuthority.DELETE_TASK)
    fun deleteTask(number: Int, projectUuid: UUID, currentUsername: String): DeleteTaskResult {
        if (!taskService.existsByProjectUuidAndNumber(number, projectUuid)) return DeleteTaskResult.TaskNotFound
        taskService.delete(number, projectUuid)
        return DeleteTaskResult.Success
    }

    private fun <T> withUserValidation(
        username: String,
        projectUuid: UUID,
        notFound: T,
        noAccess: T,
        success: () -> T
    ): T {
        return when (validateUser(username, projectUuid)) {
            UserValidation.NotFound -> notFound
            UserValidation.HasNoAccess -> noAccess
            UserValidation.Success -> success()
        }
    }

    private fun validateUser(username: String, projectUuid: UUID): UserValidation {
        return if (!userService.existsByUserName(username)) {
            UserValidation.NotFound
        } else if (!projectService.hasAccess(projectUuid, username)) {
            UserValidation.HasNoAccess
        } else UserValidation.Success
    }

    sealed interface UserValidation {
        object Success : UserValidation
        object NotFound : UserValidation
        object HasNoAccess : UserValidation
    }
}
