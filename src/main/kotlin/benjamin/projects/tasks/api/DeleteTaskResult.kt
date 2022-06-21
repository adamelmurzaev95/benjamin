package benjamin.projects.tasks.api

sealed class DeleteTaskResult {
    object ProjectNotFound : DeleteTaskResult()
    object TaskNotFound : DeleteTaskResult()
    object AccessDenied : DeleteTaskResult()
    object Success : DeleteTaskResult()
}
