package benjamin.invitation.impl

import benjamin.invitation.api.Invitation
import benjamin.invitation.api.InviteCommand
import benjamin.projects.impl.ProjectRepository
import org.springframework.beans.factory.annotation.Value
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class InvitationService(
    private val repo: InvitationRepository,
    private val projectRepository: ProjectRepository
) {
    @Value("expiration.seconds")
    private var expirationSeconds: Long = 2400

    fun save(inviteCommand: InviteCommand, sender: String): UUID {
        val savedEntity = repo.save(toEntity(inviteCommand, sender))

        return savedEntity.invitationUuid
    }

    fun getByUuid(invitationUUID: UUID): Invitation? {
        val invitation = repo.findByInvitationUuid(invitationUUID)
        return if (invitation != null) fromEntity(invitation) else null
    }

    private fun toEntity(inviteCommand: InviteCommand, sender: String): InvitationEntity {
        return InvitationEntity().apply {
            this.sender = sender
            receiver = inviteCommand.receiver
            projectRole = inviteCommand.projectRole
            invitationUuid = UUID.randomUUID()
            project = projectRepository.findByUuid(inviteCommand.projectUuid)!!
            expireAt = Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS)
        }
    }

    private fun fromEntity(inviteEntity: InvitationEntity): Invitation {
        return Invitation(
            receiver = inviteEntity.receiver,
            role = inviteEntity.projectRole,
            projectUuid = inviteEntity.project.uuid,
            expireAt = inviteEntity.expireAt
        )
    }
}
