package benjamin.rest.projects

import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.rest.models.ProjectModel
import benjamin.rest.utils.Helper.error
import benjamin.rest.utils.Helper.getUsername
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects/{projectTitle}/tasks")
class TasksRestController(
    private val projectModel: ProjectModel
) {
    @GetMapping
    fun getAllByProjectTitle(@PathVariable projectTitle: String): ResponseEntity<Any> {
        val result = projectModel.getAllTasksByProjectTitle(projectTitle)

        return when (result) {
            null -> ResponseEntity.status(HttpStatus.NOT_FOUND).error("Project with such title not found")
            else -> ResponseEntity.ok().body(result)
        }
    }

    @GetMapping("/{id}")
    fun getProfileById(@PathVariable id: Int): ResponseEntity<Any> {
        val result = projectModel.getTaskProfileById(id)

        return when (result) {
            null -> ResponseEntity.status(HttpStatus.NOT_FOUND).error("Task with such id not found")
            else -> ResponseEntity.ok().body(result)
        }
    }

    @PostMapping
    fun create(
        token: JwtAuthenticationToken,
        @PathVariable projectTitle: String,
        @RequestBody createTaskCommand: CreateTaskCommand
    ): ResponseEntity<Any> {
        val username = token.getUsername()
        val result = projectModel.createTask(username, projectTitle, createTaskCommand)

        return when (result) {
            CreateTaskResult.Success -> ResponseEntity.ok().build()
            CreateTaskResult.ProjectNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .error("Project with such title not found")
            CreateTaskResult.AssigneeNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .error("Assignee with such username not found")
        }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody updateTaskCommand: UpdateTaskCommand): ResponseEntity<Any> {
        val result = projectModel.updateTask(id, updateTaskCommand)

        return when (result) {
            UpdateTaskResult.Success -> ResponseEntity.ok().build()
            UpdateTaskResult.TaskNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .error("Task with such id not found")
            UpdateTaskResult.AssigneeNotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .error("Assignee with such username not found")
        }
    }
}
