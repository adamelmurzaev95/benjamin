package benjamin.rest.invitation

import benjamin.invitation.api.InviteCommand
import benjamin.invitation.api.InviteResult
import benjamin.rest.invitation.models.InvitationModel
import benjamin.rest.utils.Helper.error
import benjamin.rest.utils.Helper.getUsername
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
            InviteResult.ProjectNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Project with such uuid not found")
            InviteResult.ReceiverNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .error("Receiver with such username not found")
            InviteResult.AccessDenied -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .error("Access Denied")
            InviteResult.AlreadyHasAccess -> ResponseEntity.status(HttpStatus.CONFLICT)
                .error("This receiver already has access")
            is InviteResult.Success -> ResponseEntity.ok(result.uuid)
        }
    }

    @PostMapping("/join/{linkUuid}")
    fun join(@PathVariable linkUuid: UUID): ResponseEntity<Any> {
        invitationModel.join(linkUuid)
        return ResponseEntity.ok().build()
    }
}
