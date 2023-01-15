package com.volodya262.jbproductsinfo

import com.volodya262.jbproductsinfo.application.clients.JetBrainsProductInfoClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class JbProductsInfoApplicationTests(
    @Autowired val jetBrainsProductInfoClient: JetBrainsProductInfoClient
) {

    @Test
    fun contextLoads() {
    }
}
