package benjamin.projects.api

data class UpdateProjectCommand(
    val title: String? = null,
    val description: String? = null
)
