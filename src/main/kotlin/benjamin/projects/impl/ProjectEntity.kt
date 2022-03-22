package benjamin.projects.impl

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "projects")
class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "title")
    lateinit var title: String

    @Column(name = "description")
    lateinit var description: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectEntity) return false
        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }
}
