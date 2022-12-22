package benjamin.security

import benjamin.rest.utils.WebHelper.getUsername
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object SecurityUtils {
    fun getCurrentUsername(): String =
        (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).getUsername()
}
