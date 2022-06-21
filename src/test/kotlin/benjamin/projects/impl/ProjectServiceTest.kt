package benjamin.projects.impl

import benjamin.TestContainerPostgres
import benjamin.projects.api.CreateProjectCommand
import benjamin.projects.api.Project
import benjamin.projects.api.UpdateProjectCommand
import benjamin.projects.api.UpdateProjectResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest(properties = [TestContainerPostgres.url])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectServiceTest {
    @Autowired
    lateinit var projectRepository: ProjectRepository

    private lateinit var service: ProjectService

    private val createProjectCommand = CreateProjectCommand(
        "Benjamin",
        "Task tracker"
    )

    private val author = "a.elmurzaev95"

    @BeforeEach
    fun setup() {
        service = ProjectService(projectRepository)
    }

    @Test
    fun `getByUuid should return valid project`() {
        val uuid = service.create(author, createProjectCommand)

        val expected = Project(
            uuid = uuid,
            title = createProjectCommand.title,
            description = createProjectCommand.description,
            author = author
        )

        val actual = service.getByUuid(uuid)

        assertEquals(
            expected,
            actual
        )
    }

    @Test
    fun `create should save project in db`() {
        val uuid = service.create(author, createProjectCommand)

        val expected = Project(
            uuid = uuid,
            title = createProjectCommand.title,
            description = createProjectCommand.description,
            author = author
        )

        val actual = service.getByUuid(uuid)

        assertEquals(
            expected,
            actual
        )

        assertEquals(ProjectRole.OWNER, service.getRole(uuid, author))
    }

    @Test
    fun `update should return Success`() {
        val uuid = service.create(author, createProjectCommand)

        assertEquals(
            UpdateProjectResult.Success,
            service.update(uuid, UpdateProjectCommand("HRMS"))
        )

        assertEquals(
            "HRMS",
            service.getByUuid(uuid).title
        )
    }

    @Test
    fun `hasAccess should return true if user has access`() {
        val uuid = service.create(author, createProjectCommand)
        assertTrue(service.hasAccess(uuid, author))
    }

    @Test
    fun `hasAccess should return false if user doesnt have access`() {
        val uuid = service.create(author, createProjectCommand)
        assertFalse(service.hasAccess(uuid, "non-project-member"))
    }

    @Test
    fun `getRole should return null when user is not member of project`() {
        val uuid = service.create(author, createProjectCommand)
        assertEquals(
            null,
            service.getRole(uuid, "non-project-member")
        )
    }

    @Test
    fun `getRole should return user's role on this project`() {
        val uuid = service.create(author, createProjectCommand)
        assertEquals(
            ProjectRole.OWNER,
            service.getRole(uuid, author)
        )
    }

    @Test
    fun `getProjectsByUsername should empty list when user is not member of any projects`() {
        assertTrue(service.getProjectsByUsername(author).isEmpty())
    }

    @Test
    fun `getProjectsByUsername should return user's projects`() {
        val uuid1 = service.create(author, createProjectCommand)
        val uuid2 = service.create(author, createProjectCommand.copy(title = "HRMS"))

        val expected = listOf(
            Project(
                uuid1,
                createProjectCommand.title,
                createProjectCommand.description,
                author
            ),
            Project(
                uuid2,
                "HRMS",
                createProjectCommand.description,
                author
            )
        )

        val actual = service.getProjectsByUsername(author)

        assertEquals(expected, actual)
    }

    @Test
    fun `delete should delete project correctly`() {
        val uuid = service.create(author, createProjectCommand)

        service.delete(uuid)

        assertFalse(service.existsByUuid(uuid))
    }

    @Test
    fun `existsByUuid should return true if project exists`() {
        val uuid = service.create(author, createProjectCommand)

        assertTrue(service.existsByUuid(uuid))
    }
}
