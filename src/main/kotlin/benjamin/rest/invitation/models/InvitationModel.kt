package benjamin.rest.invitation.models

import benjamin.invitation.api.CreateInvitationEventCommand
import benjamin.invitation.api.InviteCommand
import benjamin.invitation.api.InviteResult
import benjamin.invitation.api.JoinResult
import benjamin.invitation.impl.InvitationEventRepository
import benjamin.invitation.impl.InvitationEventService
import benjamin.invitation.impl.InvitationRepository
import benjamin.invitation.impl.InvitationService
import benjamin.projects.api.Project
import benjamin.projects.impl.ProjectAuthority
import benjamin.projects.impl.ProjectRepository
import benjamin.projects.impl.ProjectService
import benjamin.security.ProjectChecks
import benjamin.users.api.User
import benjamin.users.impl.UserService
import benjamin.users.impl.UsersFetcher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.Instant
import java.util.UUID

@Component
class InvitationModel(
    projectRepository: ProjectRepository,
    invitationRepository: InvitationRepository,
    invitationEventRepository: InvitationEventRepository,
    usersFetcher: UsersFetcher
) {
    private val invitationService = InvitationService(invitationRepository, projectRepository)
    private val invitationEventService = InvitationEventService(invitationEventRepository)
    private val projectService = ProjectService(projectRepository)
    private val userService = UserService(usersFetcher)
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    @ProjectChecks(projectUuidPath = "inviteCommand.projectUuid", requiredAuthority = ProjectAuthority.INVITE)
    fun invite(inviteCommand: InviteCommand, author: String): InviteResult {
        val receiver = userService.getByUsername(inviteCommand.receiver)
        if (receiver == null) return InviteResult.ReceiverNotFound

        if (projectService.hasAccess(inviteCommand.projectUuid, receiver.username)) return InviteResult.AlreadyHasAccess

        val project = projectService.getByUuid(inviteCommand.projectUuid)
        val linkUuid = invitationService.save(inviteCommand, author)
        invitationEventService.save(createEvent(linkUuid, author, receiver, project))

        return InviteResult.Success(linkUuid)
    }

    @Transactional
    fun join(linkUuid: UUID, currentUsername: String): JoinResult {
        val invitation = invitationService.getByUuid(linkUuid)

        if (invitation == null) return JoinResult.InvitationNotFound
        if (invitation.expireAt.isBefore(Instant.now())) return JoinResult.InvitationExpired
        if (invitation.receiver != currentUsername) return JoinResult.AccessDenied

        projectService.addToProject(invitation.projectUuid, invitation.receiver, invitation.role)
        return JoinResult.Success
    }

    private fun createEvent(
        linkUuid: UUID,
        sender: String,
        receiver: User,
        project: Project
    ): CreateInvitationEventCommand {
        val message = buildMessage(receiver, sender, project, linkUuid)
        logger.info("Created invitation for user ${receiver.username} from $sender to project ${project.title}")
        return CreateInvitationEventCommand(
            sender = sender,
            receiverEmail = receiver.email,
            messageTopic = "Invitation to project ${project.title}",
            message = message
        )
    }

    private fun buildMessage(
        receiver: User,
        sender: String,
        project: Project,
        linkUuid: UUID
    ): String {
        return "Dear ${receiver.firstName}. $sender invites you to ${project.title} project. If you want to join follow the link ${buildUrl(linkUuid)}"
    }

    private fun buildUrl(linkUuid: UUID) = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/invitation/join/$linkUuid")
        .toUriString()
}
