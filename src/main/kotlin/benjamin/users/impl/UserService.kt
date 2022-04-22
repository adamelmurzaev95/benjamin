package benjamin.users.impl

import benjamin.users.api.UsersFetcher
import org.springframework.stereotype.Service

@Service
class UserService(
    private val usersFetcher: UsersFetcher
) {
    fun existsByUserName(username: String) =
        usersFetcher.fetchByUserName(username).isNotEmpty()
}
