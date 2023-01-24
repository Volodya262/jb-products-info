package com.volodya262.jbproductsinfo.application.listeners

import com.ninjasquad.springmockk.MockkBean
import com.volodya262.jbproductsinfo.application.services.BuildProcessorService
import com.volodya262.jbproductsinfo.domain.ProductCode
import io.mockk.every
import io.mockk.slot
import java.time.Duration
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate

@SpringBootTest
class BuildsToProcessListenerMockTest(
    @Autowired private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    @MockkBean
    lateinit var buildProcessorService: BuildProcessorService

    @Test
    fun `it should deserialize kafka message and pass expected arguments to build processor service`() {

        val productCodeSlot = slot<ProductCode>()
        val buildFullNumberSlot = slot<String>()
        every { buildProcessorService.process(capture(productCodeSlot), capture(buildFullNumberSlot)) } returns true

        kafkaTemplate.send(
            "BUILDS_TO_PROCESS",
            "IC:111.222.333",
            """
                {
                    "productCode": "IC",
                    "buildFullNumber": "111.222.333"
                }   
            """.trimIndent()
        )

        await
            .pollDelay(Duration.ofSeconds(2))
            .atMost(Duration.ofSeconds(10))
            .untilAsserted {
                Assertions.assertEquals("IC", productCodeSlot.captured)
                Assertions.assertEquals("111.222.333", buildFullNumberSlot.captured)
            }
    }
}
