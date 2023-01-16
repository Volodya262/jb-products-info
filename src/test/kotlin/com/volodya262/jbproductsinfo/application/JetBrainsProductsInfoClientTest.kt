package com.volodya262.jbproductsinfo.application

import com.volodya262.jbproductsinfo.application.clients.JetBrainsProductsInfoClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class JetBrainsProductsInfoClientTest(
    @Autowired val jetBrainsProductsInfoClient: JetBrainsProductsInfoClient
) {
    @Test
    fun foo() {
        val todayMinusYear = LocalDate.now().minusYears(1)
        jetBrainsProductsInfoClient.getProductsInfo(todayMinusYear)
    }
}