package com.volodya262.jbproductsinfo.application.clients

import com.volodya262.jbproductsinfo.application.services.DistributionDownloader
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DistributionDownloaderTest(
    @Autowired private val distributionDownloader: DistributionDownloader
) {
    @Test
    fun `should download distribution to temp file`() {
        val url = "/toolbox/jetbrains-toolbox-1.27.2.13801.tar.gz"
        distributionDownloader.downloadDistributionToTempFolder(url, "toolbox")

        // TODO assertions
    }
}