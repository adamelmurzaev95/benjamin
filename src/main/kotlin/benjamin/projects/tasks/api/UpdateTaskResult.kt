package benjamin.projects.tasks.api

sealed class UpdateTaskResult {
    object Success : UpdateTaskResult()
    object TaskNotFound : UpdateTaskResult()
    object AssigneeNotFound : UpdateTaskResult()
    object AssigneeHasNoAccess : UpdateTaskResult()
}
