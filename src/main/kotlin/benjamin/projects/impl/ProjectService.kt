package benjamin.projects.impl

import benjamin.projects.api.CreateProjectResult
import benjamin.projects.api.Project
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProjectService(
    private val repo: ProjectRepository
) {
    @Transactional(readOnly = true)
    fun getByTitle(title: String): Project? {
        val projectEntity = repo.findByTitle(title)
        return if (projectEntity != null) fromEntity(projectEntity) else null
    }

    @Transactional
    fun create(project: Project): CreateProjectResult {
        if (repo.existsByTitle(project.title)) return CreateProjectResult.AlreadyExists

        repo.save(toEntity(project))
        return CreateProjectResult.Success
    }

    @Transactional
    fun update(title: String, updateCommand: UpdateProjectCommand): UpdateProjectResult {
        val project = repo.findByTitle(title)
        if (project == null) return UpdateProjectResult.NotFound

        fillEntity(project, updateCommand)
        repo.save(project)

        return UpdateProjectResult.Success
    }

    private fun fillEntity(entity: ProjectEntity, updateCommand: UpdateProjectCommand) {
        if (updateCommand.description != null) entity.description = updateCommand.description
    }

    private fun fromEntity(projectEntity: ProjectEntity): Project {
        return Project(
            title = projectEntity.title,
            description = projectEntity.description
        )
    }

    private fun toEntity(project: Project): ProjectEntity {
        return ProjectEntity().apply {
            title = project.title
            description = project.description
        }
    }
}
