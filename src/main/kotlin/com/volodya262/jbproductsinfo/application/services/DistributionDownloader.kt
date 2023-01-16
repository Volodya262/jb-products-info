package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.domain.DistributionNotFound
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.FileOutputStream

@Component
class DistributionDownloader(
    private val distributionsRestTemplate: RestTemplate
) {
    companion object {
        const val baseUrl = "https://download.jetbrains.com"
        const val tempFilePrefix = "download-"
        const val fileName = "jetbrains-toolbox-1.27.2.13801.tar.gz"
    }

    fun downloadDistributionToTempFolder(url: String, name: String): File {
        return distributionsRestTemplate.execute<File?>(
            baseUrl + url,
            HttpMethod.GET,
            null,
            { clientHttpResponse ->
                if (clientHttpResponse.statusCode == HttpStatus.NOT_FOUND) {
                    throw DistributionNotFound(url, name)
                }

                val tempFile = File.createTempFile(tempFilePrefix, fileName)
                FileOutputStream(tempFile).use {
                    StreamUtils.copy(clientHttpResponse.body, it)
                }

                tempFile
            })!!
    }
}