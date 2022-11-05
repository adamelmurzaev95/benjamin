package benjamin.invitation.api

import java.util.UUID

sealed class InviteResult {
    object ReceiverNotFound : InviteResult()
    object AlreadyHasAccess : InviteResult()
    class Success(val uuid: UUID) : InviteResult()
}
