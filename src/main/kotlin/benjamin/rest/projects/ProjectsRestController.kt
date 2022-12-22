package benjamin.rest.projects

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.UpdateProjectCommand
import benjamin.rest.projects.models.ProjectModel
import benjamin.rest.utils.WebHelper.getUsername
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
    fun getAll(token: JwtAuthenticationToken) = projectModel.getProjectsByUsername(token.getUsername())

    @GetMapping("/{uuid}")
    fun getByUuid(@PathVariable uuid: UUID, token: JwtAuthenticationToken) =
        projectModel.getProjectByUuid(uuid, token.getUsername())

    @PostMapping
    fun create(
        token: JwtAuthenticationToken,
        @RequestBody createProjectCommand: CreateProjectCommand
    ) = projectModel.createProject(token.getUsername(), createProjectCommand)

    @PutMapping("/{uuid}")
    fun update(
        @PathVariable uuid: UUID,
        @RequestBody updateProjectCommand: UpdateProjectCommand,
        token: JwtAuthenticationToken
    ) = projectModel.updateProject(uuid, updateProjectCommand, token.getUsername())

    @DeleteMapping("/{uuid}")
    fun delete(@PathVariable uuid: UUID, token: JwtAuthenticationToken) =
        projectModel.deleteProject(uuid, token.getUsername())
}
