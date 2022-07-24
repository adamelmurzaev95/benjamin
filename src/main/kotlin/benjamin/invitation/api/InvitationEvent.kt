package benjamin.invitation.api

data class InvitationEvent(
    val eventId: Int,
    val receiverEmail: String,
    val topic: String,
    val message: String
)
