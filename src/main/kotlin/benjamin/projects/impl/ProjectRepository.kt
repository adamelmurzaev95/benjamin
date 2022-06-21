package benjamin.projects.impl

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface ProjectRepository : CrudRepository<ProjectEntity, Int> {
    fun findByUuid(uuid: UUID): ProjectEntity?

    fun existsByUuid(uuid: UUID): Boolean

    @Query(
        "SELECT * FROM projects pr INNER JOIN project_username_role rl ON pr.id = rl.project_id WHERE rl.username = ?1",
        nativeQuery = true
    )
    fun findAllByUsername(username: String): List<ProjectEntity>
}
