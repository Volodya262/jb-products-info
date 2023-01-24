package com.volodya262.jbproductsinfo.application.listeners

import com.github.tomakehurst.wiremock.client.WireMock
import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.repository.JdbcProductsRepository
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus
import com.volodya262.jbproductsinfo.domain.Product
import com.volodya262.jbproductsinfo.infrastructure.RealCurrentDateTimeProvider
import com.volodya262.libraries.testutils.getResourceAsStream
import java.time.Duration
import java.time.LocalDate
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.kafka.core.KafkaTemplate

@SpringBootTest
@AutoConfigureWireMock
class BuildsToProcessListenerTest(
    @Autowired private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Autowired private val jdbcBuildsRepository: JdbcBuildsRepository,
    @Autowired private val jdbcProductsRepository: JdbcProductsRepository,
    @Value("\${wiremock.server.port}")
    val wiremockPort: String
) {
    @BeforeEach
    fun beforeEach() {
        jdbcBuildsRepository.deleteAll()
        jdbcProductsRepository.deleteAll()
    }

    private val jsonContents = "{ \"field1\": \"value1\", \"field2\": 123 }"

    @Test
    fun `it should process build from kafka`() {
        // arrange
        val archiveStream = getResourceAsStream(this.javaClass, "archive-with-product-info.tar.gz")
        val archiveBytes = archiveStream.readAllBytes()
        WireMock.stubFor(
            WireMock.get("/file1")
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("content-type", "binary/octet-stream")
                        .withBody(archiveBytes)
                )
        )
        val product = Product("IC", "Intellij IDEA Community", emptySet())
        val buildInProcess = BuildInProcess("IC", "111.222.333", RealCurrentDateTimeProvider())
            .apply { toCreated(constructUrl("/file1"), LocalDate.now()) }
            .apply { toQueued() }

        jdbcProductsRepository.updateLocalProducts(listOf(product))
        jdbcBuildsRepository.saveNew(buildInProcess)

        // act
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

        // assert
        await
            .pollDelay(Duration.ofSeconds(5))
            .atMost(Duration.ofSeconds(30))
            .untilAsserted {
                val resultBuild = jdbcBuildsRepository.getBuild("IC", "111.222.333")
                assertEquals(BuildInProcessStatus.Processed, resultBuild.status)
                assertNotNull(resultBuild.targetFileContents)
                assertEquals(jsonContents.trim(), resultBuild.targetFileContents!!.trim())
            }
    }

    private fun constructUrl(relativePath: String) =
        "http://localhost:$wiremockPort$relativePath"
}
