package benjamin.common

import benjamin.users.api.keycloak.KeycloakProps
import benjamin.users.impl.UsersFetcherImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class BenjaminConfig(
    private val keycloakProps: KeycloakProps,
    private val registrationRepo: ClientRegistrationRepository,
    private val oauthClientRepo: OAuth2AuthorizedClientRepository
) {
    @Bean
    fun usersFetcher() = UsersFetcherImpl(oauth2WebClient())

    private fun oauth2WebClient(): WebClient {
        val oauth2 = ServletOAuth2AuthorizedClientExchangeFilterFunction(registrationRepo, oauthClientRepo)
        oauth2.setDefaultOAuth2AuthorizedClient(true)
        oauth2.setDefaultClientRegistrationId(keycloakProps.registrationId)

        return WebClient.builder()
            .apply(oauth2.oauth2Configuration())
            .baseUrl(keycloakProps.host)
            .build()
    }
}
