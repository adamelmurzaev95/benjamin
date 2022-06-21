package benjamin.projects.tasks.api

sealed class CreateTaskResult {
    class Success(val taskNum: Int) : CreateTaskResult()
    object ProjectNotFound : CreateTaskResult()
    object AccessDenied : CreateTaskResult()
    object AssigneeNotFound : CreateTaskResult()
    object AssigneeHasNoAccess : CreateTaskResult()
}
