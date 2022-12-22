package benjamin.projects.api

sealed class ChangeUserRoleResult {
    object TargetUserNotFound : ChangeUserRoleResult()
    object TargetUserHasNoAccess : ChangeUserRoleResult()
    object Success : ChangeUserRoleResult()
}
