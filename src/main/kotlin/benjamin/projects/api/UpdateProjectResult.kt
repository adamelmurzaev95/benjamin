package benjamin.projects.api

sealed class UpdateProjectResult {
    object Success : UpdateProjectResult()
    object NotFound : UpdateProjectResult()
}
