package benjamin.users

sealed class RegisterUserResult {
    object Success : RegisterUserResult()
    object AlreadyExists : RegisterUserResult()
}
