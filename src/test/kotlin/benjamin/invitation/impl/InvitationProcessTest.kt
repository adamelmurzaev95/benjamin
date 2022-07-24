package benjamin.invitation.impl

import benjamin.TestContainerPostgres
import benjamin.invitation.api.InvitationEvent
import benjamin.projects.impl.ProjectEntity
import benjamin.projects.impl.ProjectRepository
import benjamin.projects.impl.ProjectRole
import benjamin.projects.impl.ProjectUserEntity
import benjamin.users.api.User
import benjamin.users.impl.UsersFetcher
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CountDownLatch

@SpringBootTest(
    properties = [TestContainerPostgres.url]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [InvitationProcessTest.Initializer::class])
@AutoConfigureMockMvc
class InvitationProcessTest {

    companion object {
        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            kafkaContainer.start()
            TestPropertyValues.of(
                "spring.kafka.bootstrap-servers=${kafkaContainer.bootstrapServers}"
            ).applyTo(applicationContext)
        }
    }

    private fun consumer() = DefaultKafkaConsumerFactory<String, String>(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
            "key.deserializer" to StringDeserializer::class.java,
            "value.deserializer" to StringDeserializer::class.java,
            "auto.offset.reset" to "earliest",
            "group.id" to "test-group"
        )
    ).createConsumer()

    @Autowired
    private lateinit var countDownLatch: CountDownLatch

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var web: MockMvc

    @MockkBean
    private lateinit var usersFetcher: UsersFetcher

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    private val currentUser = "zumso72"

    private val invitationReceiver = User(
        username = "mansoor",
        firstName = "Mansoor",
        lastName = "Mazhiev",
        email = "mansoor@gmail.com"
    )

    private val projectEntity = ProjectEntity().apply {
        uuid = UUID.randomUUID()
        title = "benjamin"
        description = "Task tracker"
        author = currentUser
    }

    private val inviteCommandJson = """
        {
          "receiver": "mansoor",
          "projectUuid": "${projectEntity.uuid}",
          "projectRole": "USER"
        }
    """.trimIndent()

    @BeforeEach
    fun setup() {
        val savedEntity = projectRepository.save(projectEntity)
        savedEntity.users.add(
            ProjectUserEntity().apply {
                projectId = savedEntity.id; username = currentUser; role = ProjectRole.OWNER
            }
        )
        projectRepository.save(savedEntity)
    }

    @Test
    fun `invitation successful case test`() {
        usersFetcher.mockGetUser()

        val invitationUuid =
            web.post("/invitation/invite") {
                mockJwt(currentUser)
                contentType = MediaType.APPLICATION_JSON
                content = inviteCommandJson
            }.andExpect {
                status {
                    isOk()
                }
            }.andReturn().response.contentAsString.replace("\"", "")

        assertFalse(
            projectRepository.findByUuid(projectEntity.uuid)!!.users
                .any { user -> user.username == invitationReceiver.username }
        )

        countDownLatch.await()

        consumer().use { consumer ->
            val topic = "BENJAMIN.EMAIL"
            consumer.subscribe(listOf(topic))
            val records = consumer.poll(Duration.ofSeconds(1)).records(topic)

            val actual = records
                .map { it.value() }
                .map { objectMapper.readValue<InvitationEvent>(it) }
                .first()

            val expected = InvitationEvent(
                eventId = actual.eventId,
                receiverEmail = invitationReceiver.email,
                topic = "Invitation to project ${projectEntity.title}",
                message = buildMessage(invitationReceiver, currentUser, projectEntity, invitationUuid)
            )

            assertEquals(expected, actual)

            web.post("/invitation/join/$invitationUuid") {
                mockJwt(invitationReceiver.username)
            }.andExpect {
                status {
                    isOk()
                }
            }

            assertTrue(
                projectRepository.findByUuid(projectEntity.uuid)!!.users
                    .any { user -> user.username == invitationReceiver.username }
            )
        }
    }

    private fun MockHttpServletRequestDsl.mockJwt(username: String) =
        with(SecurityMockMvcRequestPostProcessors.jwt().jwt { it.claim("user_name", username) })

    private fun UsersFetcher.mockGetUser() {
        every { fetchByUserName(invitationReceiver.username) } returns listOf(invitationReceiver)
    }

    private fun buildMessage(
        receiver: User,
        sender: String,
        project: ProjectEntity,
        linkUuid: String
    ): String {
        return "Dear ${receiver.firstName}. $sender invites you to ${project.title} project. If you want to join follow the link ${
        buildUrl(
            linkUuid
        )
        }"
    }

    fun buildUrl(linkUuid: String) = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/invitation/join/$linkUuid")
        .toUriString()
}
