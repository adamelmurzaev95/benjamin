package benjamin.invitation.impl

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "invitations_outbox")
class InvitationEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    var eventId: Int? = null

    @Column(name = "receiver_email")
    lateinit var receiverEmail: String

    @Column(name = "message_topic")
    lateinit var messageTopic: String

    @Column(name = "message")
    lateinit var message: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvitationEventEntity) return false
        return eventId == other.eventId
    }

    override fun hashCode(): Int {
        return eventId.hashCode()
    }
}
