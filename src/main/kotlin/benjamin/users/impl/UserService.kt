package benjamin.users.impl

class UserService(
    private val usersFetcher: UsersFetcher
) {
    fun existsByUserName(username: String) =
        usersFetcher.fetchByUserName(username).isNotEmpty()

    fun getByUsername(username: String) = usersFetcher.fetchByUserName(username).firstOrNull()
}
