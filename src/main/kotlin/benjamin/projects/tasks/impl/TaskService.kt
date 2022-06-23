package benjamin.projects.tasks.impl

import benjamin.projects.impl.ProjectRepository
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.DeleteTaskResult
import benjamin.projects.tasks.api.Task
import benjamin.projects.tasks.api.TaskProfile
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.Tasks
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import java.time.Instant
import java.util.UUID

class TaskService(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
) {
    fun getAllByProjectUuid(projectUuid: UUID): Tasks {
        val projectEntity = projectRepo.findByUuid(projectUuid)!!

        return Tasks(
            taskRepo.getAllByProject(projectEntity)
                .sortedBy { it.creationDateTime }
                .map(this::fromEntity)
        )
    }

    fun getAllByAssigneeAndProjectUuid(assignee: String, projectUuid: UUID): Tasks {
        val projectEntity = projectRepo.findByUuid(projectUuid)
        return Tasks(taskRepo.getAllByAssigneeAndProject(assignee, projectEntity!!).map { fromEntity(it) })
    }

    fun getProfileByNumberAndProjectUuid(number: Int, projectUuid: UUID): TaskProfile {
        val projectEntity = projectRepo.findByUuid(projectUuid)!!

        return profileFromEntity(taskRepo.findByProjectAndNumber(projectEntity, number))
    }

    fun create(author: String, projectUuid: UUID, createCommand: CreateTaskCommand): Int {
        val savedEntity = taskRepo.save(toEntity(author, projectUuid, createCommand))
        return savedEntity.number!!
    }

    fun update(number: Int, projectUuid: UUID, updateCommand: UpdateTaskCommand): UpdateTaskResult {
        val projectEntity = projectRepo.findByUuid(projectUuid)!!
        val taskEntity = taskRepo.findByProjectAndNumber(projectEntity, number)

        fillEntity(taskEntity, updateCommand)
        taskRepo.save(taskEntity)

        return UpdateTaskResult.Success
    }

    fun delete(number: Int, projectUuid: UUID): DeleteTaskResult {
        val projectEntity = projectRepo.findByUuid(projectUuid)!!

        taskRepo.deleteByProjectAndNumber(projectEntity, number)

        return DeleteTaskResult.Success
    }

    fun existsByProjectUuidAndNumber(number: Int, projectUuid: UUID): Boolean {
        val projectEntity = projectRepo.findByUuid(projectUuid)!!

        return taskRepo.existsByProjectAndNumber(projectEntity, number)
    }

    fun deleteAllByProjectUuid(projectUuid: UUID) {
        val projectEntity = projectRepo.findByUuid(projectUuid)!!

        taskRepo.deleteAllByProject(projectEntity)
    }

    private fun fillEntity(taskEntity: TaskEntity, updateCommand: UpdateTaskCommand) {
        if (updateCommand.description != null) taskEntity.description = updateCommand.description
        if (updateCommand.assignee != null) taskEntity.assignee = updateCommand.assignee
        if (updateCommand.status != null) taskEntity.status = updateCommand.status

        taskEntity.changedDateTime = Instant.now()
    }

    private fun toEntity(taskAuthor: String, projectUuid: UUID, createCommand: CreateTaskCommand): TaskEntity {
        return TaskEntity().apply {
            title = createCommand.title
            description = createCommand.description
            project = projectRepo.findByUuid(projectUuid)!!
            author = taskAuthor
            assignee = createCommand.assignee
            creationDateTime = Instant.now()
            changedDateTime = Instant.now()
            status = TaskStatus.NEW
            number = (taskRepo.getMaxNumber() ?: 0) + 1
        }
    }

    private fun fromEntity(taskEntity: TaskEntity): Task {
        return Task(
            title = taskEntity.title,
            assignee = taskEntity.assignee!!,
            status = taskEntity.status,
            number = taskEntity.number!!
        )
    }

    private fun profileFromEntity(taskEntity: TaskEntity): TaskProfile {
        return TaskProfile(
            title = taskEntity.title,
            description = taskEntity.description,
            projectTitle = taskEntity.project.title,
            author = taskEntity.author,
            assignee = taskEntity.assignee,
            creationDateTime = taskEntity.creationDateTime,
            changedDateTime = taskEntity.changedDateTime,
            status = taskEntity.status,
            number = taskEntity.number!!
        )
    }
}
