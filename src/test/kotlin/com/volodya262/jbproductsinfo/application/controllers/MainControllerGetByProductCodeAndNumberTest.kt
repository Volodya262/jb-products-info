package com.volodya262.jbproductsinfo.application.controllers

import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.repository.JdbcProductsRepository
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.Product
import com.volodya262.jbproductsinfo.infrastructure.RealCurrentDateTimeProvider
import java.time.LocalDate
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerGetByProductCodeAndNumberTest(
    @Autowired val jdbcBuildsRepository: JdbcBuildsRepository,
    @Autowired val jdbcProductsRepository: JdbcProductsRepository,
    @Autowired val mockMvc: MockMvc,
) {
    @BeforeEach
    fun beforeEach() {
        jdbcBuildsRepository.deleteAll()
        jdbcProductsRepository.deleteAll()
    }

    val product1 = Product("IC", "Intellij IDEA Community", setOf("IIC"))
    val build1 = BuildInProcess("IC", "111.111.111", RealCurrentDateTimeProvider())
        .apply {
            toCreated("link1.ru", LocalDate.now())
            toQueued()
        }

    val build2 = BuildInProcess("IC", "111.111.222", RealCurrentDateTimeProvider())
        .apply {
            toCreated("link2.ru", LocalDate.now())
            toQueued()
            toProcessing()
            toProcessed("target file content")
        }

    @Test
    fun `it should return target file of a processed build`() {
        jdbcProductsRepository.updateLocalProducts(listOf(product1))
        jdbcBuildsRepository.saveNew(listOf(build1, build2))

        mockMvc.get("/IC/111.111.222")
            .andDo { print() }
            .andExpect {
                status { isOk() }
                content { string("target file content") }
            }
    }

    @Test
    fun `it should return build target file of a processed build when alternative product code was specified`() {
        jdbcProductsRepository.updateLocalProducts(listOf(product1))
        jdbcBuildsRepository.saveNew(listOf(build1, build2))

        mockMvc.get("/IIC/111.111.222") // <- alternative product code
            .andDo { print() }
            .andExpect {
                status { isOk() }
                content { string("target file content") }
            }
    }

    @Test
    fun `it should return WrongBuildProcessingStatus if build is not processed yet`() {
        jdbcProductsRepository.updateLocalProducts(listOf(product1))
        jdbcBuildsRepository.saveNew(listOf(build1, build2))

        mockMvc.get("/IC/111.111.111")
            .andDo { print() }
            .andExpect {
                status { isPreconditionFailed() }
                jsonPath("$.errorCode", equalTo("WrongBuildProcessingStatus"))
            }
    }

    @Test
    fun `it should return BuildNotFound if build not exists`() {
        jdbcProductsRepository.updateLocalProducts(listOf(product1))
        jdbcBuildsRepository.saveNew(listOf(build1, build2))

        mockMvc.get("/IC/111.111.555")
            .andDo { print() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorCode", equalTo("BuildNotFound"))
            }
    }
}
