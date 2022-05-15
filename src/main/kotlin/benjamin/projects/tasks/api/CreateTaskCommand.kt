package benjamin.projects.tasks.api

data class CreateTaskCommand(
    val title: String,
    val description: String? = null,
    val assignee: String? = null,
)
