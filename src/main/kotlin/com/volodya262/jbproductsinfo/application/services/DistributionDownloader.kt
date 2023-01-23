package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.DistributionDownloadError
import com.volodya262.jbproductsinfo.domain.DistributionNotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.web.client.RestTemplate
import java.io.OutputStream

@Component
@Profile("default", "default_worker", "test")
class DistributionDownloader(
    private val distributionsRestTemplate: RestTemplate
) {

    val logger: Logger = LoggerFactory.getLogger(DistributionDownloader::class.java)

    fun downloadDistributionToOutputStream(build: BuildInProcess, outputStream: OutputStream) {
        logger.info("Started downloading file for build {}", build)

        try {
            distributionsRestTemplate.execute<Unit>(
                build.downloadUrl!!,
                HttpMethod.GET,
                null,
                { clientHttpResponse ->
                    if (clientHttpResponse.statusCode == HttpStatus.NOT_FOUND) {
                        throw DistributionNotFound(build.productCode, build.buildFullNumber, build.downloadUrl!!)
                    }

                    if (clientHttpResponse.statusCode.isError) {
                        throw DistributionDownloadError(build, clientHttpResponse.statusCode.name)
                    }

                    StreamUtils.copy(clientHttpResponse.body, outputStream)
                    return@execute
                }
            )
        } catch (ex: Exception) {
            logger.error("Failed to download file for build {}", build, ex)
            throw DistributionDownloadError(build, null)
        }

        logger.info("Downloaded file for build {}", build)
    }
}
