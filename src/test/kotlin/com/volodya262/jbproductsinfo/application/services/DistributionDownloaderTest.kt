package com.volodya262.jbproductsinfo.application.services

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.infrastructure.RealCurrentDateTimeProvider
import com.volodya262.libraries.testutils.getResourceAsStream
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Arrays
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock

@SpringBootTest
@AutoConfigureWireMock
class DistributionDownloaderTest(
    @Autowired val distributionDownloader: DistributionDownloader,
    @Value("\${wiremock.server.port}")
    val wiremockPort: String
) {
    @Test
    fun `it should download archive from remote`() {
        // arrange
        val archiveStream = getResourceAsStream(this.javaClass, "archive-with-product-info.tar.gz")
        val archiveBytes = archiveStream.readAllBytes()
        WireMock.stubFor(
            WireMock.get("/file1")
                .willReturn(
                    aResponse()
                        .withHeader("content-type", "binary/octet-stream")
                        .withBody(archiveBytes)
                )
        )

        val buildInProcess = BuildInProcess("IC", "111.222.333", RealCurrentDateTimeProvider())
            .apply { toCreated(constructUrl("/file1"), LocalDate.now()) }

        val outputStream = ByteArrayOutputStream()

        // act
        distributionDownloader.downloadDistributionToOutputStream(buildInProcess, outputStream)

        // assert
        val outputStreamBytes = outputStream.toByteArray()
        assertTrue(Arrays.equals(archiveBytes, outputStreamBytes))
    }

    private fun constructUrl(relativePath: String) =
        "http://localhost:$wiremockPort$relativePath"
}
