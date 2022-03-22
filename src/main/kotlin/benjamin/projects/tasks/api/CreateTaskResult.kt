package benjamin.projects.tasks.api

sealed class CreateTaskResult {
    class Success(val id: Int) : CreateTaskResult()
    object ProjectNotFound : CreateTaskResult()
    object AssigneeNotFound : CreateTaskResult()
}
