package benjamin.rest.projects

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.DeleteProjectResult
import benjamin.projects.api.GetProjectByUuidResult
import benjamin.projects.api.Projects
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
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
@RequestMapping("/projects")
class ProjectsRestController(
    private val projectModel: ProjectModel
) {
    @GetMapping
    fun getAll(token: JwtAuthenticationToken): ResponseEntity<Projects> {
        return ResponseEntity.ok(projectModel.getProjectsByUsername(token.getUsername()))
    }

    @GetMapping("/{uuid}")
    fun getByUuid(@PathVariable uuid: UUID, token: JwtAuthenticationToken): ResponseEntity<Any> {
        val result = projectModel.getProjectByUuid(uuid, token.getUsername())

        return when (result) {
            GetProjectByUuidResult.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such title not found")
            GetProjectByUuidResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            is GetProjectByUuidResult.Success -> ResponseEntity.ok(result.project)
        }
    }

    @PostMapping
    fun create(
        token: JwtAuthenticationToken,
        @RequestBody createProjectCommand: CreateProjectCommand
    ): ResponseEntity<Any> {
        val author = token.getUsername()
        val result = projectModel.createProject(author, createProjectCommand)

        return ResponseEntity.ok(result)
    }

    @PutMapping("/{uuid}")
    fun update(
        @PathVariable uuid: UUID,
        @RequestBody updateProjectCommand: UpdateProjectCommand,
        token: JwtAuthenticationToken
    ): ResponseEntity<Any> {
        val result = projectModel.updateProject(uuid, updateProjectCommand, token.getUsername())

        return when (result) {
            UpdateProjectResult.Success -> ResponseEntity.ok().build()
            UpdateProjectResult.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            UpdateProjectResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
        }
    }

    @DeleteMapping("/{uuid}")
    fun delete(@PathVariable uuid: UUID, token: JwtAuthenticationToken): ResponseEntity<Any> {
        val result = projectModel.deleteProject(uuid, token.getUsername())

        return when (result) {
            DeleteProjectResult.Success -> ResponseEntity.ok().build()
            DeleteProjectResult.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            DeleteProjectResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
        }
    }
}
