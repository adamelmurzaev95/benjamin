package benjamin.security

import benjamin.projects.impl.ProjectAuthority
import benjamin.projects.impl.ProjectRepository
import benjamin.projects.impl.ProjectService
import benjamin.rest.utils.getAnnotation
import benjamin.rest.utils.getArgument
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import java.util.LinkedList
import java.util.UUID
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProjectChecks(
    val projectUuidPath: String,
    val accessRequired: Boolean = true,
    val requiredAuthority: ProjectAuthority = ProjectAuthority.SEE_PROJECT
)

class ProjectAccessDeniedException(message: String) : RuntimeException(message)
class ProjectNotFoundException(message: String) : RuntimeException(message)

class ProjectAccessChecker(private val projectService: ProjectService) {
    fun checkAccess(projectUuid: UUID, authority: ProjectAuthority): Boolean {
        return projectService.getRole(projectUuid, SecurityUtils.getCurrentUsername())?.authorities
            ?.contains(authority) == true
    }
}

object ProjectUuidExtractor {
    fun extractProjectUuid(projectUuidPath: String, joinPoint: JoinPoint): UUID {
        return if (!projectUuidPath.containsDot()) {
            joinPoint.getArgument(projectUuidPath) as UUID
        } else {
            val pathParts = LinkedList(projectUuidPath.split("."))
            val argument = joinPoint.getArgument(pathParts[0])
            getProjectUuidRecursively(
                argument,
                pathParts.apply {
                    remove()
                }
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getProjectUuidRecursively(argument: Any, pathParts: MutableList<String>): UUID {
        val property = argument::class.memberProperties.first { it.name == pathParts[0] } as KProperty1<Any, *>
        val propertyValue = property.get(argument)!!
        return if (pathParts.size == 1) {
            propertyValue as UUID
        } else {
            getProjectUuidRecursively(
                propertyValue,
                pathParts.apply {
                    removeFirst()
                }
            )
        }
    }

    private fun String.containsDot() = "." in this
}

@Component
@Aspect
class ProjectChecksAspect(
    projectRepository: ProjectRepository
) {
    private val projectService = ProjectService(projectRepository)
    private val projectAccessChecker = ProjectAccessChecker(projectService)

    @Before("@annotation(benjamin.security.ProjectChecks)")
    fun projectChecksAdvice(joinPoint: JoinPoint) {
        val projectsCheck = joinPoint.getAnnotation(ProjectChecks::class.java)
        val projectUuidPath = projectsCheck.projectUuidPath

        val projectUuid = ProjectUuidExtractor.extractProjectUuid(projectUuidPath, joinPoint)

        if (!projectService.existsByUuid(projectUuid)) {
            throw ProjectNotFoundException("Project with uuid $projectUuid not found")
        }
        if (projectsCheck.accessRequired
            && !projectAccessChecker.checkAccess(projectUuid, projectsCheck.requiredAuthority)
        ) {
            throw ProjectAccessDeniedException("Access denied")
        }
    }
}
