package benjamin.invitation.api

sealed class JoinResult {
    object InvitationNotFound : JoinResult()
    object Success : JoinResult()
}
