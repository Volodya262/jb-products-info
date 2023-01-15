package com.volodya262.jbproductsinfo.application

import com.volodya262.jbproductsinfo.application.clients.JetBrainsProductInfoClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JetBrainsProductInfoClientTest(
    @Autowired val jetBrainsProductInfoClient: JetBrainsProductInfoClient
) {
    @Test
    fun foo() {
        jetBrainsProductInfoClient.getProductInfo()
    }
}