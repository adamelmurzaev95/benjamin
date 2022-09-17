package benjamin.invitation.impl

import benjamin.invitation.api.InvitationEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@ExtendWith(MockitoExtension::class)
@Testcontainers
class EventsMonitorTest {
    companion object {
        @Container
        val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
    }

    private val props = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
    )

    private val kafkaTemplate = KafkaTemplate(DefaultKafkaProducerFactory<String, String>(props))

    private val eventsEntities = listOf(
        InvitationEventEntity().apply { eventId = 22; receiverEmail = "a@gmail.com"; messageTopic = ""; message = "Hi" },
        InvitationEventEntity().apply { eventId = 23; receiverEmail = "b@gmail.com"; messageTopic = ""; message = "Lol" }
    )

    private val events = listOf(
        InvitationEvent(22, "a@gmail.com", "", "Hi"),
        InvitationEvent(23, "b@gmail.com", "", "Lol")
    )

    private fun consumer() = DefaultKafkaConsumerFactory<String, String>(
        mapOf(
            *props.entries.map { it.key to it.value }.toTypedArray(),
            "key.deserializer" to StringDeserializer::class.java,
            "value.deserializer" to StringDeserializer::class.java,
            "auto.offset.reset" to "earliest",
            "group.id" to "test-group"
        )
    ).createConsumer()

    private val mapper = ObjectMapper().findAndRegisterModules()

    @Mock
    lateinit var invitationEventRepository: InvitationEventRepository

    @Test
    fun `sendEvents should work correctly`() {
        Mockito.`when`(invitationEventRepository.findAll()).thenReturn(eventsEntities)

        consumer().use { consumer ->
            val topic = "BENJAMIN.EMAIL"
            consumer.subscribe(listOf(topic))
            EventsMonitor(kafkaTemplate, invitationEventRepository).sendEvents()

            val records = consumer.poll(Duration.ofSeconds(1)).records(topic)

            assertEquals(
                listOf("22", "23"),
                records.map { it.key() }
            )

            assertEquals(
                events,
                records.map { mapper.readValue<InvitationEvent>(it.value()) }
            )
        }
    }
}
