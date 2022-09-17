package benjamin.invitation.api

sealed class JoinResult {
    object InvitationNotFound : JoinResult()
    object InvitationExpired : JoinResult()
    object Success : JoinResult()
    object AccessDenied : JoinResult()
}
