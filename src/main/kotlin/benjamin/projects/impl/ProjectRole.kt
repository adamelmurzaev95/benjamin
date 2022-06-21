package benjamin.projects.impl

enum class ProjectRole(val authorities: List<ProjectAuthority>) {
    USER(
        listOf(
            ProjectAuthority.CREATE_TASK,
            ProjectAuthority.UPDATE_TASK
        )
    ),
    ADMIN(
        listOf(
            ProjectAuthority.UPDATE_PROJECT,
            ProjectAuthority.CREATE_TASK,
            ProjectAuthority.UPDATE_TASK,
            ProjectAuthority.DELETE_TASK
        )
    ),
    OWNER(ProjectAuthority.values().toList())
}
