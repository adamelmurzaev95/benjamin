package benjamin.projects.tasks.impl

import benjamin.projects.impl.ProjectEntity
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.TaskStatusToPostgreEnum
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "tasks")
@TypeDef(
    name = "pgsql_enum",
    typeClass = TaskStatusToPostgreEnum::class
)
class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "title")
    lateinit var title: String

    @Column(name = "description")
    var description: String? = null

    @ManyToOne
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectEntity

    @Column(name = "creation_date_time")
    lateinit var creationDateTime: Instant

    @Column(name = "last_modified_date_time")
    lateinit var changedDateTime: Instant

    @Column(name = "author")
    lateinit var author: String

    @Column(name = "assignee")
    var assignee: String? = null

    @Column(name = "status")
    @Type(type = "pgsql_enum")
    lateinit var status: TaskStatus

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TaskEntity) return false
        return title == other.title
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }
}
