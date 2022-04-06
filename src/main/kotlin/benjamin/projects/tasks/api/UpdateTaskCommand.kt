package benjamin.projects.tasks.api

data class UpdateTaskCommand(
    val description: String? = null,
    val assignee: String? = null,
    val status: TaskStatus? = null
)
