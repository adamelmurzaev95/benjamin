package benjamin.projects.impl

import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

enum class ProjectAuthority {
    SEE_PROJECT,
    UPDATE_PROJECT,
    DELETE_PROJECT,
    CREATE_TASK,
    UPDATE_TASK,
    DELETE_TASK,
    INVITE,
    ASSIGN_ROLES
}

enum class ProjectRole(val authorities: Set<ProjectAuthority>) {
    USER(
        setOf(
            ProjectAuthority.SEE_PROJECT,
            ProjectAuthority.CREATE_TASK,
            ProjectAuthority.UPDATE_TASK
        )
    ),
    ADMIN(
        setOf(
            ProjectAuthority.SEE_PROJECT,
            ProjectAuthority.UPDATE_PROJECT,
            ProjectAuthority.CREATE_TASK,
            ProjectAuthority.UPDATE_TASK,
            ProjectAuthority.DELETE_TASK,
            ProjectAuthority.INVITE,
            ProjectAuthority.ASSIGN_ROLES
        )
    ),
    OWNER(ProjectAuthority.values().toSet())
}

@Entity
@Table(name = "projects")
class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "uuid")
    @Type(type = "pg-uuid")
    lateinit var uuid: UUID

    @Column(name = "title")
    lateinit var title: String

    @Column(name = "description")
    lateinit var description: String

    @Column(name = "author")
    lateinit var author: String

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    val users: MutableList<ProjectUserEntity> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectEntity) return false
        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }
}
