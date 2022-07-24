package benjamin.invitation.impl

import benjamin.invitation.api.CreateInvitationEventCommand
import benjamin.invitation.api.InvitationEvent

class InvitationEventService(
    private val invitationEventRepository: InvitationEventRepository
) {
    fun save(createEventCommand: CreateInvitationEventCommand) {
        invitationEventRepository.save(toEntity(createEventCommand))
    }

    fun getAll(): List<InvitationEvent> {
        return invitationEventRepository.findAll().map { fromEntity(it) }
    }

    fun deleteById(id: Int) {
        invitationEventRepository.deleteById(id)
    }

    private fun fromEntity(entity: InvitationEventEntity): InvitationEvent {
        return InvitationEvent(
            eventId = entity.eventId!!,
            receiverEmail = entity.receiverEmail,
            topic = entity.messageTopic,
            message = entity.message
        )
    }

    private fun toEntity(createEventCommand: CreateInvitationEventCommand): InvitationEventEntity {
        return InvitationEventEntity().apply {
            receiverEmail = createEventCommand.receiverEmail
            messageTopic = createEventCommand.messageTopic
            message = createEventCommand.message
        }
    }
}
