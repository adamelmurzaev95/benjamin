package benjamin.projects.tasks.impl

import benjamin.projects.impl.ProjectEntity
import org.springframework.data.repository.CrudRepository

interface TaskRepository : CrudRepository<TaskEntity, Int> {
    fun existsByTitle(title: String): Boolean

    fun getAllByProject(projectEntity: ProjectEntity): List<TaskEntity>
}
