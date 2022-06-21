package benjamin.rest.utils

import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object Helper {
    fun JwtAuthenticationToken.getUsername() =
        token.getClaim<String>("user_name")

    fun ResponseEntity.BodyBuilder.error(message: String): ResponseEntity<Any> {
        return body(mapOf("error" to message))
    }
}
