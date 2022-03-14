package benjamin.projects

sealed class UpdateProjectResult {
    object Success : UpdateProjectResult()
    object NotFound : UpdateProjectResult()
}
