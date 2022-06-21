package benjamin.projects.tasks.api

sealed class GetTasksByAssigneeUserAndProjectUuid {
    object ProjectNotFound : GetTasksByAssigneeUserAndProjectUuid()
    object AccessDenied : GetTasksByAssigneeUserAndProjectUuid()
    class Success(val tasks: Tasks) : GetTasksByAssigneeUserAndProjectUuid()
}
