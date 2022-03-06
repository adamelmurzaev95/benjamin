package benjamin.users

data class RegisterUserCommand(
    val userName: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)
