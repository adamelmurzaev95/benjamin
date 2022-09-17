package benjamin.invitation.impl

import benjamin.projects.impl.ProjectEntity
import benjamin.projects.impl.ProjectRole
import benjamin.projects.impl.ProjectRoleToPostgreEnum
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "invitations")
@TypeDef(
    name = "pgsql_role_enum",
    typeClass = ProjectRoleToPostgreEnum::class
)
class InvitationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null

    @Column(name = "sender")
    lateinit var sender: String

    @Column(name = "receiver")
    lateinit var receiver: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectEntity

    @Column(name = "role")
    @Type(type = "pgsql_role_enum")
    lateinit var projectRole: ProjectRole

    @Column(name = "invitation_uuid")
    @Type(type = "pg-uuid")
    lateinit var invitationUuid: UUID

    @Column(name = "expiration_date")
    lateinit var expireAt: Instant

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvitationEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
