package benjamin.rest.models

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.DeleteProjectResult
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.impl.ProjectService
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.projects.tasks.impl.TaskService
import benjamin.users.impl.UserService
import org.springframework.stereotype.Component

@Component
class ProjectModel(
    private val projectService: ProjectService,
    private val taskService: TaskService,
    private val userService: UserService
) {
    fun getProjectByTitle(title: String) = projectService.getByTitle(title)

    fun createProject(author: String, createProjectCommand: CreateProjectCommand) =
        projectService.create(author, createProjectCommand)

    fun updateProject(title: String, updateProjectCommand: UpdateProjectCommand) =
        projectService.update(title, updateProjectCommand)

    fun deleteProject(title: String): DeleteProjectResult = projectService.delete(title)

    fun getAllTasksByProjectTitle(projectTitle: String) = taskService.getAllByProjectTitle(projectTitle)

    fun getTaskProfileById(id: Int) = taskService.getProfileById(id)

    fun createTask(author: String, projectTitle: String, createTaskCommand: CreateTaskCommand): CreateTaskResult {
        if (!projectService.existsByTitle(projectTitle)) return CreateTaskResult.ProjectNotFound

        val assignee = createTaskCommand.assignee
        if (assignee != null && !userService.existsByUserName(assignee)) return CreateTaskResult.AssigneeNotFound

        taskService.create(author, projectTitle, createTaskCommand)
        return CreateTaskResult.Success
    }

    fun updateTask(id: Int, updateTaskCommand: UpdateTaskCommand): UpdateTaskResult {
        val assignee = updateTaskCommand.assignee
        if (assignee != null && !userService.existsByUserName(assignee)) return UpdateTaskResult.AssigneeNotFound

        return taskService.update(id, updateTaskCommand)
    }
}
