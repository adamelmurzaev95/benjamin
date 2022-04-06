package benjamin.projects.api

sealed class CreateProjectResult {
    object Success : CreateProjectResult()
    object AlreadyExists : CreateProjectResult()
}
