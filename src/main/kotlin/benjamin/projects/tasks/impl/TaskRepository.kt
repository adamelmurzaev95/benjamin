package benjamin.projects.tasks.impl

import benjamin.projects.impl.ProjectEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface TaskRepository : CrudRepository<TaskEntity, Int> {
    fun existsByProjectAndNumber(projectEntity: ProjectEntity, number: Int): Boolean

    fun getAllByProject(projectEntity: ProjectEntity): List<TaskEntity>

    fun findByProjectAndNumber(projectEntity: ProjectEntity, number: Int): TaskEntity

    @Query("SELECT MAX(number) FROM tasks", nativeQuery = true)
    fun getMaxNumber(): Int?

    fun deleteByProjectAndNumber(projectEntity: ProjectEntity, number: Int)

    fun getAllByAssigneeAndProject(assignee: String, projectEntity: ProjectEntity): List<TaskEntity>
}
