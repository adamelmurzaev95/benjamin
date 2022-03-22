package benjamin.projects.tasks.impl

import benjamin.projects.impl.ProjectRepository
import benjamin.projects.tasks.api.CreateTaskCommand
import benjamin.projects.tasks.api.CreateTaskResult
import benjamin.projects.tasks.api.Task
import benjamin.projects.tasks.api.TaskProfile
import benjamin.projects.tasks.api.TaskStatus
import benjamin.projects.tasks.api.Tasks
import benjamin.projects.tasks.api.UpdateTaskCommand
import benjamin.projects.tasks.api.UpdateTaskResult
import benjamin.users.impl.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class TaskService(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
    private val userService: UserService
) {
    @Transactional(readOnly = true)
    fun getAllByProjectTitle(projectTitle: String): Tasks? {
        val projectEntity = projectRepo.findByTitle(projectTitle)
        if (projectEntity == null) return null

        return Tasks(
            taskRepo.getAllByProject(projectEntity)
                .sortedBy { it.creationDateTime }
                .map(this::fromEntity)
        )
    }

    @Transactional(readOnly = true)
    fun getProfileById(id: Int): TaskProfile? {
        return taskRepo.findById(id)
            .map { profileFromEntity(it) }
            .orElse(null)
    }

    @Transactional
    fun create(author: String, createCommand: CreateTaskCommand): CreateTaskResult {
        val projectTitle = createCommand.projectTitle
        val assignee = createCommand.assignee

        if (!projectRepo.existsByTitle(projectTitle)) {
            return CreateTaskResult.ProjectNotFound
        }

        if (assignee != null && userService.getByUserName(assignee) == null) {
            return CreateTaskResult.AssigneeNotFound
        }

        val savedEntity = taskRepo.save(toEntity(author, createCommand))
        return CreateTaskResult.Success(savedEntity.id!!)
    }

    @Transactional
    fun update(id: Int, updateCommand: UpdateTaskCommand): UpdateTaskResult {
        val taskEntity = taskRepo.findById(id).orElse(null)

        if (taskEntity == null) return UpdateTaskResult.TaskNotFound

        val assignee = updateCommand.assignee

        if (assignee != null && userService.getByUserName(assignee) == null) {
            return UpdateTaskResult.AssigneeNotFound
        }

        fillEntity(taskEntity, updateCommand)
        taskRepo.save(taskEntity)

        return UpdateTaskResult.Success
    }

    private fun fillEntity(taskEntity: TaskEntity, updateCommand: UpdateTaskCommand) {
        if (updateCommand.description != null) taskEntity.description = updateCommand.description
        if (updateCommand.assignee != null) taskEntity.assignee = updateCommand.assignee
        if (updateCommand.status != null) taskEntity.status = updateCommand.status

        taskEntity.changedDateTime = Instant.now()
    }

    private fun toEntity(taskAuthor: String, createCommand: CreateTaskCommand): TaskEntity {
        return TaskEntity().apply {
            title = createCommand.title
            description = createCommand.description
            project = projectRepo.findByTitle(createCommand.projectTitle)!!
            author = taskAuthor
            assignee = createCommand.assignee
            creationDateTime = Instant.now()
            changedDateTime = Instant.now()
            status = TaskStatus.NEW
        }
    }

    private fun fromEntity(taskEntity: TaskEntity): Task {
        return Task(
            title = taskEntity.title,
            assignee = taskEntity.assignee!!,
            status = taskEntity.status
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
            status = taskEntity.status
        )
    }
}
