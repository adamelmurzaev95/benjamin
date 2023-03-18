package benjamin.invitation.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EventsMonitor(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    invitationEventRepository: InvitationEventRepository,
) {
    private val invitationEventService = InvitationEventService(invitationEventRepository)
    private val topic = "BENJAMIN.EMAIL"
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper().apply {
        findAndRegisterModules()
    }

    @Scheduled(fixedDelayString = "\${invitationsMonitor.delay}")
    fun sendEvents() {
        invitationEventService.getAll()
            .map { Pair(ProducerRecord(topic, null, it.eventId.toString(), mapper.writeValueAsString(it)), it.eventId) }
            .forEach {
                try {
                    kafkaTemplate.send(it.first).get()
                    logger.info("${it.second} is sent to Kafka")
                    invitationEventService.deleteById(it.second)
                } catch (e: Exception) {
                    logger.error("Failed to process record with event id {}", it.second)
                }
            }
    }
}
