package benjamin.projects

sealed class CreateProjectResult {
    object Success : CreateProjectResult()
    object AlreadyExists : CreateProjectResult()
}
