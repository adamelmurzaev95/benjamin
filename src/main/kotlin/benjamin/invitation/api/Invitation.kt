package benjamin.invitation.api

import benjamin.projects.impl.ProjectRole
import java.time.Instant
import java.util.UUID

data class Invitation(
    val receiver: String,
    val role: ProjectRole,
    val projectUuid: UUID,
    val expireAt: Instant
)
