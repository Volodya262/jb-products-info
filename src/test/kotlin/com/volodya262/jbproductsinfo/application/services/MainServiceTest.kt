package com.volodya262.jbproductsinfo.application.services

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MainServiceTest(
    @Autowired val mainService: MainService
) {
    @Test
    fun foo() { // TODO remove me
        mainService.doStuff()
    }
}