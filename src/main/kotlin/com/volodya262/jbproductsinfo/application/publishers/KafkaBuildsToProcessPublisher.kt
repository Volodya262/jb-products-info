package com.volodya262.jbproductsinfo.application.publishers

import com.fasterxml.jackson.databind.ObjectMapper
import com.volodya262.jbproductsinfo.application.dto.BuildToProcessKafkaMessage
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaBuildsToProcessPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaBuildsToProcessPublisher::class.java)

    fun publish(build: List<BuildInProcess>) {
        build.forEach { publish(it) }
    }

    fun publish(build: BuildInProcess) {
        val message = BuildToProcessKafkaMessage.from(build)
        val kafkaKey = message.generateKafkaKey()

        try {
            logger.info("Publishing build to kafka. build: $build; message: $message")
            kafkaTemplate.send(
                "BUILDS_TO_PROCESS",
                kafkaKey,
                objectMapper.writeValueAsString(message)
            )
        } catch (ex: Exception) {
            logger.error("Failed to publish build to kafka. build: $build", ex)
            throw ex
        }
    }
}
