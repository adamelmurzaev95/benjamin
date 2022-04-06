package benjamin.projects.tasks.api

import java.time.Instant

data class TaskProfile(
    val title: String,
    val description: String?,
    val projectTitle: String,
    val author: String,
    val assignee: String?,
    val creationDateTime: Instant,
    val changedDateTime: Instant,
    val status: TaskStatus
)
