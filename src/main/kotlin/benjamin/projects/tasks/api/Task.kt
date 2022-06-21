package benjamin.projects.tasks.api

data class Task(
    val title: String,
    val assignee: String,
    val status: TaskStatus,
    val number: Int
)
