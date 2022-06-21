package benjamin.projects.tasks.api

sealed class GetTasksByProjectUuid {
    object ProjectNotFound : GetTasksByProjectUuid()
    object AccessDenied : GetTasksByProjectUuid()
    class Success(val tasks: Tasks) : GetTasksByProjectUuid()
}
