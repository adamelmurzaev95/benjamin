package benjamin.rest.handlers

import benjamin.rest.utils.WebHelper.error
import benjamin.security.ProjectAccessDeniedException
import benjamin.security.ProjectNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ProjectsExceptionHandler {
    @ExceptionHandler(ProjectAccessDeniedException::class)
    fun handleAccessDenied() = ResponseEntity.status(HttpStatus.FORBIDDEN).error("Access denied")

    @ExceptionHandler(ProjectNotFoundException::class)
    fun handleProjectNotFound(ex: ProjectNotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).error(ex.message)
}
