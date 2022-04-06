package benjamin.projects

import benjamin.projects.api.CreateProjectResult
import benjamin.projects.api.Project
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
import benjamin.projects.impl.ProjectService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectService::class)
class ProjectServiceTest {
    @Autowired
    private lateinit var service: ProjectService

    private val project = Project(
        "Benjamin",
        "Task tracker"
    )

    @Test
    fun `getByTitle should return null if no project with such title exists`() {
        assertEquals(
            null,
            service.getByTitle("Benjamin")
        )
    }

    @Test
    fun `getByTitle should return valid project`() {

        service.create(project)

        assertEquals(
            project,
            service.getByTitle("Benjamin")
        )
    }

    @Test
    fun `create should return AlreadyExists if project with such title exists`() {

        service.create(project)

        assertEquals(
            CreateProjectResult.AlreadyExists,
            service.create(project)
        )
    }

    @Test
    fun `create should return Success if project with such title doesnt exist`() {

        assertEquals(
            CreateProjectResult.Success,
            service.create(project)
        )
    }

    @Test
    fun `update should return NofFound if no project was updated`() {
        assertEquals(
            UpdateProjectResult.NotFound,
            service.update("Benjamin", UpdateProjectCommand("My project"))
        )
    }

    @Test
    fun `update should return Success`() {
        service.create(project)

        assertEquals(
            UpdateProjectResult.Success,
            service.update("Benjamin", UpdateProjectCommand("My project"))
        )
    }
}
