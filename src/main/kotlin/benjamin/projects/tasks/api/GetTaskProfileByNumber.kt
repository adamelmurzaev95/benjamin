package benjamin.projects.tasks.api

sealed class GetTaskProfileByNumber {
    object TaskNotFound : GetTaskProfileByNumber()
    class Success(val taskProfile: TaskProfile) : GetTaskProfileByNumber()
}
