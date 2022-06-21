package benjamin.projects.impl

import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "project_username_role")
@TypeDef(
    name = "pgsql_role_enum",
    typeClass = ProjectRoleToPostgreEnum::class
)
class ProjectUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    var id: Int? = null

    @Column(name = "project_id")
    var projectId: Int? = null

    @Column(name = "username")
    lateinit var username: String

    @Column(name = "role")
    @Type(type = "pgsql_role_enum")
    lateinit var role: ProjectRole

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectUserEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
