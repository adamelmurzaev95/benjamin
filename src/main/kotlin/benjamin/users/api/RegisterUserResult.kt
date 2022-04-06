package benjamin.users.api

sealed class RegisterUserResult {
    object Success : RegisterUserResult()
    object AlreadyExists : RegisterUserResult()
}
