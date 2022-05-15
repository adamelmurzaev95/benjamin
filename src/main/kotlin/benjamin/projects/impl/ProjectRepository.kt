package benjamin.projects.impl

import org.springframework.data.repository.CrudRepository

interface ProjectRepository : CrudRepository<ProjectEntity, Int> {
    fun findByTitle(title: String): ProjectEntity?

    fun existsByTitle(title: String): Boolean

    fun deleteByTitle(title: String)
}
