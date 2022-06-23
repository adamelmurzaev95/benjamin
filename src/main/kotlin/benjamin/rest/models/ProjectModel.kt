package benjamin.rest.models

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.DeleteProjectResult
import benjamin.projects.api.GetProjectByUuidResult
import benjamin.projects.api.Projects
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
import benjamin.projects.impl.ProjectAuthority
import benjamin.projects.impl.ProjectRepository
import benjamin.projects.impl.ProjectService
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.DeleteTaskResult
import benjamin.projects.tasks.api.GetTaskProfileByNumber
import benjamin.projects.tasks.api.GetTasksByAssigneeUserAndProjectUuid
import benjamin.projects.tasks.api.GetTasksByProjectUuid
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.projects.tasks.impl.TaskRepository
import benjamin.projects.tasks.impl.TaskService
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
    fun getProjectByUuid(uuid: UUID, currentUsername: String): GetProjectByUuidResult {
        if (!projectService.existsByUuid(uuid)) return GetProjectByUuidResult.NotFound
        if (!projectService.hasAccess(uuid, currentUsername)) return GetProjectByUuidResult.AccessDenied

        return GetProjectByUuidResult.Success(projectService.getByUuid(uuid))
    }

    @Transactional
    fun createProject(author: String, createProjectCommand: CreateProjectCommand): UUID {
        return projectService.create(author, createProjectCommand)
    }

    @Transactional
    fun updateProject(
        uuid: UUID,
        updateProjectCommand: UpdateProjectCommand,
        currentUsername: String
    ): UpdateProjectResult {
        if (!projectService.existsByUuid(uuid)) return UpdateProjectResult.NotFound

        val currentUserRole = projectService.getRole(uuid, currentUsername)
        if (currentUserRole == null || ProjectAuthority.UPDATE_PROJECT !in currentUserRole.authorities)
            return UpdateProjectResult.AccessDenied

        return projectService.update(uuid, updateProjectCommand)
    }

    @Transactional
    fun deleteProject(uuid: UUID, currentUsername: String): DeleteProjectResult {
        if (!projectService.existsByUuid(uuid)) return DeleteProjectResult.NotFound

        val currentUserRole = projectService.getRole(uuid, currentUsername)
        if (currentUserRole == null || ProjectAuthority.DELETE_PROJECT !in currentUserRole.authorities)
            return DeleteProjectResult.AccessDenied

        taskService.deleteAllByProjectUuid(uuid)
        return projectService.delete(uuid)
    }

    @Transactional(readOnly = true)
    fun getProjectsByUsername(username: String): Projects {
        return Projects(projectService.getProjectsByUsername(username))
    }

    @Transactional(readOnly = true)
    fun getAllTasksByProjectUuid(projectUuid: UUID, currentUsername: String): GetTasksByProjectUuid {
        if (!projectService.existsByUuid(projectUuid)) return GetTasksByProjectUuid.ProjectNotFound
        if (!projectService.hasAccess(projectUuid, currentUsername)) return GetTasksByProjectUuid.AccessDenied

        return GetTasksByProjectUuid.Success(taskService.getAllByProjectUuid(projectUuid))
    }

    @Transactional(readOnly = true)
    fun getTaskProfileByNumber(number: Int, projectUuid: UUID, currentUsername: String): GetTaskProfileByNumber {
        if (!projectService.existsByUuid(projectUuid)) return GetTaskProfileByNumber.ProjectNotFound
        if (!taskService.existsByProjectUuidAndNumber(number, projectUuid)) return GetTaskProfileByNumber.TaskNotFound
        if (!projectService.hasAccess(projectUuid, currentUsername)) return GetTaskProfileByNumber.AccessDenied

        return GetTaskProfileByNumber.Success(taskService.getProfileByNumberAndProjectUuid(number, projectUuid))
    }

    @Transactional
    fun getTasksByAssigneeAndProjectUuid(
        assignee: String,
        projectUuid: UUID
    ): GetTasksByAssigneeUserAndProjectUuid {
        if (!projectService.existsByUuid(projectUuid)) return GetTasksByAssigneeUserAndProjectUuid.ProjectNotFound
        if (!projectService.hasAccess(projectUuid, assignee)) return GetTasksByAssigneeUserAndProjectUuid.AccessDenied

        val tasks = taskService.getAllByAssigneeAndProjectUuid(assignee, projectUuid)
        return GetTasksByAssigneeUserAndProjectUuid.Success(tasks)
    }

    @Transactional
    fun createTask(author: String, projectUuid: UUID, createTaskCommand: CreateTaskCommand): CreateTaskResult {
        if (!projectService.existsByUuid(projectUuid)) return CreateTaskResult.ProjectNotFound

        val currentUserRole = projectService.getRole(projectUuid, author)
        if (currentUserRole == null || ProjectAuthority.CREATE_TASK !in currentUserRole.authorities)
            return CreateTaskResult.AccessDenied

        val assignee = createTaskCommand.assignee
        if (assignee != null) {
            if (!userService.existsByUserName(assignee)) return CreateTaskResult.AssigneeNotFound
            if (!projectService.hasAccess(projectUuid, assignee)) return CreateTaskResult.AssigneeHasNoAccess
        }

        val taskNum = taskService.create(author, projectUuid, createTaskCommand)
        return CreateTaskResult.Success(taskNum)
    }

    @Transactional
    fun updateTask(
        number: Int,
        projectUuid: UUID,
        currentUsername: String,
        updateTaskCommand: UpdateTaskCommand
    ): UpdateTaskResult {
        if (!projectService.existsByUuid(projectUuid)) return UpdateTaskResult.ProjectNotFound
        if (!taskService.existsByProjectUuidAndNumber(number, projectUuid)) return UpdateTaskResult.TaskNotFound

        val currentUserRole = projectService.getRole(projectUuid, currentUsername)
        if (currentUserRole == null || ProjectAuthority.UPDATE_TASK !in currentUserRole.authorities)
            return UpdateTaskResult.AccessDenied

        val assignee = updateTaskCommand.assignee
        if (assignee != null) {
            if (!userService.existsByUserName(assignee)) return UpdateTaskResult.AssigneeNotFound
            if (!projectService.hasAccess(projectUuid, assignee)) return UpdateTaskResult.AssigneeHasNoAccess
        }

        return taskService.update(number, projectUuid, updateTaskCommand)
    }

    @Transactional
    fun deleteTask(number: Int, projectUuid: UUID, currentUsername: String): DeleteTaskResult {
        if (!projectService.existsByUuid(projectUuid)) return DeleteTaskResult.ProjectNotFound
        if (!taskService.existsByProjectUuidAndNumber(number, projectUuid)) return DeleteTaskResult.TaskNotFound

        val currentUserRole = projectService.getRole(projectUuid, currentUsername)
        if (currentUserRole == null || ProjectAuthority.DELETE_TASK !in currentUserRole.authorities)
            return DeleteTaskResult.AccessDenied

        return taskService.delete(number, projectUuid)
    }
}
