package benjamin.rest.projects

import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.DeleteTaskResult
import benjamin.projects.tasks.api.GetTaskProfileByNumber
import benjamin.projects.tasks.api.GetTasksByProjectUuid
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.rest.models.ProjectModel
import benjamin.rest.utils.Helper.error
import benjamin.rest.utils.Helper.getUsername
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/projects/{projectUuid}/tasks")
class TasksRestController(
    private val projectModel: ProjectModel
) {
    @GetMapping
    fun getAllByProjectUuid(@PathVariable projectUuid: UUID, token: JwtAuthenticationToken): ResponseEntity<Any> {
        val result = projectModel.getAllTasksByProjectUuid(projectUuid, token.getUsername())

        return when (result) {
            GetTasksByProjectUuid.ProjectNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            GetTasksByProjectUuid.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            is GetTasksByProjectUuid.Success -> ResponseEntity.ok(result.tasks)
        }
    }

    @GetMapping("/{number}")
    fun getProfileByNumber(
        @PathVariable number: Int,
        @PathVariable projectUuid: UUID,
        token: JwtAuthenticationToken
    ): ResponseEntity<Any> {
        val result = projectModel.getTaskProfileByNumber(number, projectUuid, token.getUsername())

        return when (result) {
            GetTaskProfileByNumber.TaskNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Task with such number not found")
            GetTaskProfileByNumber.ProjectNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            GetTaskProfileByNumber.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            is GetTaskProfileByNumber.Success -> ResponseEntity.ok(result.taskProfile)
        }
    }

    @PostMapping
    fun create(
        token: JwtAuthenticationToken,
        @PathVariable projectUuid: UUID,
        @RequestBody createTaskCommand: CreateTaskCommand
    ): ResponseEntity<Any> {
        val username = token.getUsername()
        val result = projectModel.createTask(username, projectUuid, createTaskCommand)

        return when (result) {
            CreateTaskResult.ProjectNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            CreateTaskResult.AssigneeNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Assignee with such username not found")
            CreateTaskResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            CreateTaskResult.AssigneeHasNoAccess -> ResponseEntity.status(HttpStatus.CONFLICT)
                .error("Assignee hasn't access to this project")
            is CreateTaskResult.Success -> ResponseEntity.ok(result.taskNum)
        }
    }

    @PutMapping("/{number}")
    fun update(
        token: JwtAuthenticationToken,
        @PathVariable number: Int,
        @PathVariable projectUuid: UUID,
        @RequestBody updateTaskCommand: UpdateTaskCommand
    ): ResponseEntity<Any> {
        val result = projectModel.updateTask(number, projectUuid, token.getUsername(), updateTaskCommand)

        return when (result) {
            UpdateTaskResult.TaskNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Task with such number not found")
            UpdateTaskResult.AssigneeNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Assignee with such username not found")
            UpdateTaskResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            UpdateTaskResult.ProjectNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            UpdateTaskResult.AssigneeHasNoAccess -> ResponseEntity.status(HttpStatus.CONFLICT)
                .error("Assignee hasn't access to this project")
            UpdateTaskResult.Success -> ResponseEntity.ok().build()
        }
    }

    @DeleteMapping("/{number}")
    fun delete(
        @PathVariable number: Int,
        @PathVariable projectUuid: UUID,
        token: JwtAuthenticationToken
    ): ResponseEntity<Any> {
        val result = projectModel.deleteTask(number, projectUuid, token.getUsername())

        return when (result) {
            DeleteTaskResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            DeleteTaskResult.ProjectNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            DeleteTaskResult.TaskNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Task with such number not found")
            DeleteTaskResult.Success -> ResponseEntity.ok().build()
        }
    }
}
