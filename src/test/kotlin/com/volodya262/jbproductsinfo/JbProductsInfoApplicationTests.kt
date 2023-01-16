package com.volodya262.jbproductsinfo

import com.volodya262.jbproductsinfo.application.clients.JetBrainsDataServicesClient
import com.volodya262.jbproductsinfo.application.clients.JetBrainsProductsInfoClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate


@SpringBootTest
class JbProductsInfoApplicationTests(
    @Autowired val jetBrainsProductsInfoClient: JetBrainsProductsInfoClient,
    @Autowired val jetBrainsDataServicesClient: JetBrainsDataServicesClient
) {

    @Test
    fun contextLoads() {
    }

    @Test
    fun foo() {
        val todayMinusYear = LocalDate.now().minusYears(1)
        jetBrainsDataServicesClient.getProductCodeToBuildDownloadInfosMap(todayMinusYear)
    }
}
