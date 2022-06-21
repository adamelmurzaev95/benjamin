package benjamin.projects.api

sealed class GetProjectByUuidResult {
    class Success(val project: Project) : GetProjectByUuidResult()
    object NotFound : GetProjectByUuidResult()
    object AccessDenied : GetProjectByUuidResult()
}
