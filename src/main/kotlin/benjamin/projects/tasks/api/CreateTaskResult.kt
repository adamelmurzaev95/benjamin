package benjamin.projects.tasks.api

sealed class CreateTaskResult {
    object Success : CreateTaskResult()
    object ProjectNotFound : CreateTaskResult()
    object AssigneeNotFound : CreateTaskResult()
}
