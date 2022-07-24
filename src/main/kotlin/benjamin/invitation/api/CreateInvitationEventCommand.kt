package benjamin.invitation.api

data class CreateInvitationEventCommand(
    val sender: String,
    val receiverEmail: String,
    val messageTopic: String,
    val message: String
)
