package benjamin.rest.projects

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.CreateProjectResult
import benjamin.projects.api.DeleteProjectResult
import benjamin.projects.api.Project
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

@RestController
@RequestMapping("/projects")
class ProjectsRestController(
    private val projectModel: ProjectModel
) {
    @GetMapping("/{title}")
    fun getByTitle(@PathVariable title: String): ResponseEntity<Project> {
        val result = projectModel.getProjectByTitle(title)

        return when (result) {
            null -> ResponseEntity.notFound().build()
            else -> ResponseEntity.ok().body(result)
        }
    }

    @PostMapping
    fun create(
        token: JwtAuthenticationToken,
        @RequestBody createProjectCommand: CreateProjectCommand
    ): ResponseEntity<Any> {
        val author = token.getUsername()
        val result = projectModel.createProject(author, createProjectCommand)

        return when (result) {
            CreateProjectResult.Success -> ResponseEntity.ok().build()
            CreateProjectResult.AlreadyExists ->
                ResponseEntity.status(HttpStatus.CONFLICT)
                    .error("Project with such title already exists")
        }
    }

    @PutMapping("/{title}")
    fun update(
        @PathVariable title: String,
        @RequestBody updateProjectCommand: UpdateProjectCommand
    ): ResponseEntity<Any> {
        val result = projectModel.updateProject(title, updateProjectCommand)

        return when (result) {
            UpdateProjectResult.Success -> ResponseEntity.ok().build()
            UpdateProjectResult.NotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .error("Project with such title not found")
        }
    }

    @DeleteMapping("/{title}")
    fun delete(@PathVariable title: String): ResponseEntity<Any> {
        val result = projectModel.deleteProject(title)

        return when (result) {
            DeleteProjectResult.Success -> ResponseEntity.ok().build()
            DeleteProjectResult.NotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .error("Project with such title not found")
        }
    }
}
