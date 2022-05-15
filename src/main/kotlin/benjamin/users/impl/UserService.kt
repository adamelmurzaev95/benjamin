package benjamin.users.impl

import org.springframework.stereotype.Component

@Component
class UserService(
    private val usersFetcher: UsersFetcher
) {
    fun existsByUserName(username: String) =
        usersFetcher.fetchByUserName(username).isNotEmpty()
}
