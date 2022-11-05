package benjamin.projects.tasks.api

sealed class CreateTaskResult {
    class Success(val taskNum: Int) : CreateTaskResult()
    object AssigneeNotFound : CreateTaskResult()
    object AssigneeHasNoAccess : CreateTaskResult()
}
