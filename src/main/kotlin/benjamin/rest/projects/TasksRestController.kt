package benjamin.rest.projects

import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.DeleteTaskResult
import benjamin.projects.tasks.api.GetTaskProfileByNumber
import benjamin.projects.tasks.api.Tasks
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.rest.projects.models.ProjectModel
import benjamin.rest.utils.WebHelper.error
import benjamin.rest.utils.WebHelper.getUsername
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
    fun getAllByProjectUuid(@PathVariable projectUuid: UUID, token: JwtAuthenticationToken): Tasks {
        return projectModel.getAllTasksByProjectUuid(projectUuid, token.getUsername())
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
            CreateTaskResult.AssigneeNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Assignee with such username not found")
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
            DeleteTaskResult.TaskNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Task with such number not found")
            DeleteTaskResult.Success -> ResponseEntity.ok().build()
        }
    }
}
