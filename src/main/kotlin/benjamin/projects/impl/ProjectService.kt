package benjamin.projects.impl

import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.DeleteProjectResult
import benjamin.projects.api.Project
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
import java.util.UUID

class ProjectService(
    private val repo: ProjectRepository,
) {
    fun getByUuid(uuid: UUID): Project {
        val projectEntity = repo.findByUuid(uuid)!!
        return fromEntity(projectEntity)
    }

    fun create(author: String, createProjectCommand: CreateProjectCommand): UUID {
        val savedEntity = repo.save(toEntity(author, createProjectCommand))
        savedEntity.users.add(
            ProjectUserEntity().apply {
                projectId = savedEntity.id
                username = author
                role = ProjectRole.OWNER
            }
        )
        return savedEntity.uuid
    }

    fun hasAccess(uuid: UUID, username: String): Boolean {
        return repo.findByUuid(uuid)!!.users.any { it.username == username }
    }

    fun getRole(uuid: UUID, username: String): ProjectRole? {
        return repo.findByUuid(uuid)!!.users.firstOrNull { it.username == username }?.role
    }

    fun getProjectsByUsername(username: String): List<Project> {
        return repo.findAllByUsername(username).map { fromEntity(it) }
    }

    fun update(uuid: UUID, updateCommand: UpdateProjectCommand): UpdateProjectResult {
        val projectEntity = repo.findByUuid(uuid)!!

        fillEntity(projectEntity, updateCommand)
        repo.save(projectEntity)

        return UpdateProjectResult.Success
    }

    fun delete(uuid: UUID): DeleteProjectResult {
        val projectEntity = repo.findByUuid(uuid)!!

        repo.delete(projectEntity)
        return DeleteProjectResult.Success
    }

    fun existsByUuid(uuid: UUID) = repo.existsByUuid(uuid)

    fun addToProject(uuid: UUID, targetUsername: String, targetRole: ProjectRole) {
        val projectEntity = repo.findByUuid(uuid)!!
        projectEntity.users.add(
            ProjectUserEntity().apply {
                projectId = projectEntity.id
                username = targetUsername
                role = targetRole
            }
        )
    }

    private fun fillEntity(entity: ProjectEntity, updateCommand: UpdateProjectCommand) {
        if (updateCommand.title != null) entity.title = updateCommand.title
        if (updateCommand.description != null) entity.description = updateCommand.description
    }

    private fun fromEntity(projectEntity: ProjectEntity): Project {
        return Project(
            uuid = projectEntity.uuid,
            title = projectEntity.title,
            description = projectEntity.description,
            author = projectEntity.author
        )
    }

    private fun toEntity(username: String, createProjectCommand: CreateProjectCommand): ProjectEntity {
        return ProjectEntity().apply {
            uuid = UUID.randomUUID()
            title = createProjectCommand.title
            description = createProjectCommand.description
            author = username
        }
    }
}
