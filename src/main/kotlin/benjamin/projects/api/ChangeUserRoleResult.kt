package benjamin.projects.api

sealed class ChangeUserRoleResult {
    object AccessDenied : ChangeUserRoleResult()
    object ProjectNotFound : ChangeUserRoleResult()
    object TargetUserNotFound : ChangeUserRoleResult()
    object TargetUserHasNoAccess : ChangeUserRoleResult()
    object Success : ChangeUserRoleResult()
}
