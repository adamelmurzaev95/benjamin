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
