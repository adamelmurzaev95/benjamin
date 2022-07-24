package benjamin.projects.api

import benjamin.projects.impl.ProjectRole

data class ChangeUserRoleCommand(
    val userName: String,
    val role: ProjectRole
)
