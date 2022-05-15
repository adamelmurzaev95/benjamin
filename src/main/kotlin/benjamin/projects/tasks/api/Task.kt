package benjamin.projects.tasks.api

data class Task(
    val id: Int,
    val title: String,
    val assignee: String,
    val status: TaskStatus
)
