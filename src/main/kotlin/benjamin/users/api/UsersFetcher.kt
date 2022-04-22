package benjamin.users.api

interface UsersFetcher {
    fun fetchByUserName(username: String): List<User>
}
