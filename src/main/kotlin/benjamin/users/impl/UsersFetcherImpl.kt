package benjamin.users.impl

import benjamin.users.api.User
import benjamin.users.api.UsersFetcher
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

class UsersFetcherImpl(
    private val webClient: WebClient
) : UsersFetcher {
    override fun fetchByUserName(username: String): List<User> {
        return webClient.get()
            .uri("/users?username=$username&exact=true")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<User>>()
            .block()!!
    }
}
