package benjamin.rest.invitation

import benjamin.invitation.api.InviteCommand
import benjamin.invitation.api.InviteResult
import benjamin.invitation.api.JoinResult
import benjamin.rest.invitation.models.InvitationModel
import benjamin.rest.utils.WebHelper.error
import benjamin.rest.utils.WebHelper.getUsername
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/invitation")
class InvitationController(
    private val invitationModel: InvitationModel
) {
    @PostMapping("/invite")
    fun invite(@RequestBody inviteCommand: InviteCommand, token: JwtAuthenticationToken): ResponseEntity<Any> {
        val result = invitationModel.invite(inviteCommand, token.getUsername())
        return when (result) {
            InviteResult.ReceiverNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Receiver with such username not found")
            InviteResult.AlreadyHasAccess -> ResponseEntity.status(HttpStatus.CONFLICT)
                .error("This receiver already has access")
            is InviteResult.Success -> ResponseEntity.ok(result.uuid)
        }
    }

    @PostMapping("/join/{linkUuid}")
    fun join(@PathVariable linkUuid: UUID, token: JwtAuthenticationToken): ResponseEntity<Any> {
        return when (invitationModel.join(linkUuid, token.getUsername())) {
            JoinResult.InvitationNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("No invitation with such uuid found")
            JoinResult.InvitationExpired -> ResponseEntity.status(HttpStatus.GONE)
                .error("Your invitation already expired")
            JoinResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            JoinResult.Success -> ResponseEntity.ok().build()
        }
    }
}
