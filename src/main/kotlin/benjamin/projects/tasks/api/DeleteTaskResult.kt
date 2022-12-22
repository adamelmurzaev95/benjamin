package benjamin.projects.tasks.api

sealed class DeleteTaskResult {
    object TaskNotFound : DeleteTaskResult()
    object Success : DeleteTaskResult()
}
