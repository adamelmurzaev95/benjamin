package benjamin.users.impl

import benjamin.users.api.User

interface UsersFetcher {
    fun fetchByUserName(username: String): List<User>
}
