package benjamin.projects.tasks.api

data class UpdateTaskCommand(
    val title: String? = null,
    val description: String? = null,
    val assignee: String? = null,
    val status: TaskStatus? = null
)
