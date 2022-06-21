package benjamin.projects.api

import java.util.UUID

data class Project(
    val uuid: UUID,
    val title: String,
    val description: String,
    val author: String
)
