package benjamin.users.keycloak

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig(
    @Value("\${keycloak.host}") private val host: String,
    @Value("\${oauth2.registrationId}") private val registrationId: String
) {
    @Bean
    fun keycloakProps() = KeycloakProps(host, registrationId)
}
