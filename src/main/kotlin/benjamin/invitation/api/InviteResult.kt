package benjamin.invitation.api

import java.util.UUID

sealed class InviteResult {
    object ProjectNotFound : InviteResult()
    object ReceiverNotFound : InviteResult()
    object AccessDenied : InviteResult()
    object AlreadyHasAccess : InviteResult()
    class Success(val uuid: UUID) : InviteResult()
}
