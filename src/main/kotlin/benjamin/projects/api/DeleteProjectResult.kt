package benjamin.projects.api

sealed class DeleteProjectResult {
    object NotFound : DeleteProjectResult()
    object Success : DeleteProjectResult()
    object AccessDenied : DeleteProjectResult()
}
