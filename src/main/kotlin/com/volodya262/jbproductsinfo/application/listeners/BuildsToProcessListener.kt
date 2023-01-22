package com.volodya262.jbproductsinfo.application.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import com.volodya262.jbproductsinfo.application.dto.BuildToProcessKafkaMessage
import com.volodya262.jbproductsinfo.application.services.BuildProcessorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class BuildsToProcessListener(
    val objectMapper: ObjectMapper,
    val buildProcessorService: BuildProcessorService,
    @Value("\${kafka.builds-to-process.nack-sleep-time-seconds}")
    val nackSleepTimeSeconds: Long
) {

    val logger: Logger = LoggerFactory.getLogger(BuildsToProcessListener::class.java)

    @KafkaListener(topics = ["BUILDS_TO_PROCESS"], concurrency = "\${kafka.builds-to-process.concurrency}")
    fun onBuildToProcess(
        @Payload payload: String,
        @Headers headers: MessageHeaders,
        acknowledgment: Acknowledgment
    ) {
        val logMessage = "Received kafka message. headers: $headers; payload: $payload"
        logger.info(logMessage)

        val (productCode, buildFullNumber) = try {
            objectMapper.readValue(payload, BuildToProcessKafkaMessage::class.java)!!
        } catch (ex: Exception) {
            logger.error("Failed to deserialize object $payload")
            // dead letter queue is a better strategy for this case
            acknowledgment.nack(Duration.ofSeconds(nackSleepTimeSeconds))
            return
        }

        try {
            val shouldAck = buildProcessorService.process(productCode, buildFullNumber)
            if (shouldAck) {
                acknowledgment.acknowledge()
            } else {
                acknowledgment.nack(Duration.ofSeconds(nackSleepTimeSeconds))
            }
        } catch (ex: Exception) {
            logger.error(
                "Unknown error occurred during build processing. (productCode: {}; buildFullNumber: {})",
                productCode,
                buildFullNumber,
                ex
            )
        }
    }
}
