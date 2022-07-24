package benjamin.invitation.api

import benjamin.projects.impl.ProjectRole
import java.util.UUID

data class InviteCommand(
    val receiver: String,
    val projectUuid: UUID,
    val projectRole: ProjectRole
)
