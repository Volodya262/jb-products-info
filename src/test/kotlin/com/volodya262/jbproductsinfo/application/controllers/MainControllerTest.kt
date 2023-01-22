package com.volodya262.jbproductsinfo.application.controllers

import com.ninjasquad.springmockk.MockkBean
import com.volodya262.jbproductsinfo.application.publishers.KafkaBuildsToProcessPublisher
import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.repository.JdbcProductsRepository
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus
import com.volodya262.jbproductsinfo.domain.CurrentDateTimeProvider
import com.volodya262.libraries.testextensions.stubForJsonGet
import com.volodya262.libraries.testextensions.stubForXmlGet
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureWireMock
@AutoConfigureMockMvc
class MainControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val jdbcBuildsRepository: JdbcBuildsRepository,
    @Autowired val jdbcProductsRepository: JdbcProductsRepository
) {

    @BeforeEach
    fun beforeEach() {
        jdbcBuildsRepository.deleteAll()
        jdbcProductsRepository.deleteAll()
        every { currentDateTimeProvider.getLocalDate() } answers { LocalDate.parse("2023-01-22", DateTimeFormatter.ISO_DATE) }
        every { currentDateTimeProvider.getOffsetDateTime() } answers { OffsetDateTime.parse("2023-01-22T05:00Z") }
    }

    @MockkBean(relaxed = true)
    lateinit var kafkaBuildsToProcessPublisher: KafkaBuildsToProcessPublisher

    @MockkBean
    lateinit var currentDateTimeProvider: CurrentDateTimeProvider

    @Test
    fun `given empty db it should publish all builds and save them to db`() {
        // arrange
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            twoProductsWithSimpleCodesXml
        }

        stubForJsonGet("/data-services/products") {
            twoProductsWithSimpleCodesJson
        }

        val publisherArgSlot = slot<List<BuildInProcess>>()
        every { kafkaBuildsToProcessPublisher.publish(capture(publisherArgSlot)) } answers {}

        // act
        mockMvc.post("/refresh").andExpect { status { isOk() } }

        // assert
        val publishedBuilds = publisherArgSlot.captured
        assertThat(publishedBuilds, hasSize(4))
        assertTrue(publishedBuilds.any { it.productCode == "DS" } && publishedBuilds.any { it.productCode == "CL" })
        assertTrue(publishedBuilds.all { it.status == BuildInProcessStatus.Queued })

        val buildsInDb = jdbcBuildsRepository.getBuilds()
        assertThat(buildsInDb, hasSize(4))
        assertTrue(buildsInDb.any { it.productCode == "DS" } && buildsInDb.any { it.productCode == "CL" })
        assertTrue(buildsInDb.all { it.status == BuildInProcessStatus.Queued })
    }

    @Test
    fun `given filled db it shouldn't publish tasks again`() {
        // arrange
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            twoProductsWithSimpleCodesXml
        }

        stubForJsonGet("/data-services/products") {
            twoProductsWithSimpleCodesJson
        }

        every { kafkaBuildsToProcessPublisher.publish(any<List<BuildInProcess>>()) } answers {}
        mockMvc.post("/refresh").andExpect { status { isOk() } }
        val buildsInDb = jdbcBuildsRepository.getBuilds()
        assertThat(buildsInDb, hasSize(4))
        // act
        mockMvc.post("/refresh").andExpect { status { isOk() } }
        verify(exactly = 1) { kafkaBuildsToProcessPublisher.publish(any<List<BuildInProcess>>()) }
    }

    @Test
    fun `given empty db it should publish tasks only of specified product`() {
        // arrange
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            twoProductsWithSimpleCodesXml
        }

        stubForJsonGet("/data-services/products") {
            twoProductsWithSimpleCodesJson
        }

        val publisherArgSlot = slot<List<BuildInProcess>>()
        every { kafkaBuildsToProcessPublisher.publish(capture(publisherArgSlot)) } answers {}

        // act
        mockMvc.post("/refresh/CL").andExpect { status { isOk() } }

        // assert
        val buildsInDb = jdbcBuildsRepository.getBuilds()
        assertThat(buildsInDb, hasSize(2))
        assertTrue { buildsInDb.all { it.productCode == "CL" } }

        val publishedBuilds = publisherArgSlot.captured
        assertThat(publishedBuilds, hasSize(2))
        assertTrue { publishedBuilds.all { it.productCode == "CL" } }
    }

    @Test
    fun `given db with old builds it should queue only new builds`() {
        // arrange
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            oneProductSimpleCodeV1Xml
        }

        stubForJsonGet("/data-services/products") {
            oneProductSimpleCodeV1Json
        }

        val publisherArgSlot = slot<List<BuildInProcess>>()
        every { kafkaBuildsToProcessPublisher.publish(capture(publisherArgSlot)) } answers {}
        mockMvc.post("/refresh/CL").andExpect { status { isOk() } }
        assertThat(publisherArgSlot.captured, hasSize(1))

        stubForXmlGet("/jetbrains-updates/updates.xml") {
            oneProductSimpleCodeV2Xml
        }

        stubForJsonGet("/data-services/products") {
            oneProductSimpleCodeV2Json
        }

        // act
        mockMvc.post("/refresh/CL").andExpect { status { isOk() } }

        // assert
        val publishedBuilds = publisherArgSlot.captured
        assertThat(publishedBuilds, hasSize(1))
        assertTrue { publishedBuilds.find { it.buildFullNumber == "223.8214.51"} != null }

        val buildsInDb = jdbcBuildsRepository.getBuilds()
        assertThat(buildsInDb, hasSize(2))
    }

    @Test
    fun `given empty db it should record product check`() {
        // arrange
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            twoProductsWithSimpleCodesXml
        }

        stubForJsonGet("/data-services/products") {
            twoProductsWithSimpleCodesJson
        }

        val currentLocalDate = LocalDate.parse("2023-01-22", DateTimeFormatter.ISO_DATE)
        val currentOffsetDateTime = OffsetDateTime.parse("2023-01-22T05:00Z")
        every { currentDateTimeProvider.getLocalDate() } answers { currentLocalDate }
        every { currentDateTimeProvider.getOffsetDateTime() } answers { currentOffsetDateTime }
        every { kafkaBuildsToProcessPublisher.publish(any<List<BuildInProcess>>()) } answers {}

        // act
        mockMvc.post("/refresh").andExpect { status { isOk() } }

        // assert
        val products = jdbcProductsRepository.getProducts()
        assertThat(products, hasSize(2))
        assertTrue {products.find { it.productCode == "CL" }!!.lastUpdate.isEqual(currentOffsetDateTime)}
        assertTrue {products.find { it.productCode == "DS" }!!.lastUpdate.isEqual(currentOffsetDateTime)}
    }

    @Test
    fun `given processed build it should record only specified product check`() {
        // arrange
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            twoProductsWithSimpleCodesXml
        }

        stubForJsonGet("/data-services/products") {
            twoProductsWithSimpleCodesJson
        }

        val currentLocalDate = LocalDate.parse("2023-01-22", DateTimeFormatter.ISO_DATE)
        val currentOffsetDateTime = OffsetDateTime.parse("2023-01-22T05:00Z")
        every { currentDateTimeProvider.getLocalDate() } answers { currentLocalDate }
        every { currentDateTimeProvider.getOffsetDateTime() } answers { currentOffsetDateTime }
        every { kafkaBuildsToProcessPublisher.publish(any<List<BuildInProcess>>()) } answers {}
        mockMvc.post("/refresh").andExpect { status { isOk() } }

        val newCurrentLocalDate = currentLocalDate.plusDays(1)
        val newCurrentOffsetDateTime = currentOffsetDateTime.plusDays(1)
        every { currentDateTimeProvider.getLocalDate() } answers { newCurrentLocalDate }
        every { currentDateTimeProvider.getOffsetDateTime() } answers { newCurrentOffsetDateTime }

        // act
        mockMvc.post("/refresh/CL").andExpect { status { isOk() } }

        // assert
        val products = jdbcProductsRepository.getProducts()
        assertThat(products, hasSize(2))
        assertTrue {products.find { it.productCode == "CL" }!!.lastUpdate.isEqual(newCurrentOffsetDateTime)}
        assertTrue {products.find { it.productCode == "DS" }!!.lastUpdate.isEqual(currentOffsetDateTime)}
    }
}

@Language("JSON")
private val oneProductSimpleCodeV1Json =
    """
    [
      {
        "code": "CL",
        "name": "CLion",
        "releases": [
          {
            "date": "2022-11-30",
            "type": "release",
            "version": "2022.3",
            "majorVersion": "2022.3",
            "downloads": {
              "windows": {
                "link": "https://download.jetbrains.com/cpp/CLion-2022.3.exe",
                "size": 668489488,
                "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.exe.sha256"
              },
              "linux": {
                "link": "https://download.jetbrains.com/cpp/CLion-2022.3.tar.gz",
                "size": 835387407,
                "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.tar.gz.sha256"
              }
            },
            "licenseRequired": true,
            "build": "223.7571.171"
          }
        ],
        "forSale": true,
        "productFamilyName": "CLion",
        "intellijProductCode": "CL",
        "alternativeCodes": [
          "CL"
        ],
        "salesCode": "CL",
        "link": "https://www.jetbrains.com/clion",
        "description": "A cross-platform C and C++ IDE",
        "types": [
          {
            "id": "code",
            "name": "Code"
          }
        ],
        "categories": [
          "IDE"
        ]
      }
    ]
    """.trimIndent()

private val oneProductSimpleCodeV1Xml =
    """
        <products>
            <product name="CLion">
                <code>CL</code>
                <channel id="CL-RELEASE-licensing-RELEASE" name="CLion RELEASE" status="release"
                         url="https://www.jetbrains.com/clion/download" feedback="https://youtrack.jetbrains.com"
                         majorVersion="2022" licensing="release">
                    <build number="223.7571" version="2022.3" releaseDate="20221130" fullNumber="223.7571.171">
                        <blogPost url="https://www.jetbrains.com/clion/whatsnew/"/>
                        <patch from="223.7571" size="from 215 to 343" fullFrom="223.7571.113"/>
                        <patch from="222.4345" size="from 545 to 681" fullFrom="222.4345.21"/>
                    </build>
                </channel>
            </product>
        </products>
    """.trimIndent()

@Language("JSON")
val oneProductSimpleCodeV2Json =
    """
        [
          {
            "code": "CL",
            "name": "CLion",
            "releases": [
              {
                "date": "2022-12-21",
                "type": "release",
                "version": "2022.3.1",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.1.exe",
                    "size": 700077360,
                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.1.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.1.tar.gz",
                    "size": 846981199,
                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.1.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/CPP-A-230654194",
                "licenseRequired": true,
                "build": "223.8214.51"
              },
              {
                "date": "2022-11-30",
                "type": "release",
                "version": "2022.3",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.exe",
                    "size": 668489488,
                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.tar.gz",
                    "size": 835387407,
                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.tar.gz.sha256"
                  }
                },
                "licenseRequired": true,
                "build": "223.7571.171"
              }
            ],
            "forSale": true,
            "productFamilyName": "CLion",
            "intellijProductCode": "CL",
            "alternativeCodes": [
              "CL"
            ],
            "salesCode": "CL",
            "link": "https://www.jetbrains.com/clion",
            "description": "A cross-platform C and C++ IDE",
            "types": [
              {
                "id": "code",
                "name": "Code"
              }
            ],
            "categories": [
              "IDE"
            ]
          }
        ]
    """.trimIndent()

private val oneProductSimpleCodeV2Xml =
    """
        <products>
            <product name="CLion">
                <code>CL</code>
                <channel id="CL-RELEASE-licensing-RELEASE" name="CLion RELEASE" status="release"
                         url="https://www.jetbrains.com/clion/download" feedback="https://youtrack.jetbrains.com"
                         majorVersion="2022" licensing="release">
                    <build number="223.8214" version="2022.3.1" releaseDate="20221130" fullNumber="223.8214.51">
                        <blogPost
                                url="https://blog.jetbrains.com/clion/2022/12/looking-forward-towards-2023-in-clion"/>
                        <message>bla</message>
                        <button name="Download" url="https://www.jetbrains.com/clion/download" download="true"/>
                        <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/CPP-A-230654194"/>
                        <button name="More Information"
                                url="https://blog.jetbrains.com/clion/2022/12/looking-forward-towards-2023-in-clion"/>
                        <patch from="223.7571" size="from 243 to 384" fullFrom="223.7571.171"/>
                    </build>
                    <build number="223.7571" version="2022.3" releaseDate="20221130" fullNumber="223.7571.171">
                        <blogPost url="https://www.jetbrains.com/clion/whatsnew/"/>
                        <patch from="223.7571" size="from 215 to 343" fullFrom="223.7571.113"/>
                        <patch from="222.4345" size="from 545 to 681" fullFrom="222.4345.21"/>
                    </build>
                </channel>
            </product>
        </products>
    """.trimIndent()

@Language("JSON")
private val twoProductsWithSimpleCodesJson =
    """
            [
                {
                    "code": "CL",
                    "name": "CLion",
                    "releases": [
                        {
                            "date": "2022-12-21",
                            "type": "release",
                            "version": "2022.3.1",
                            "majorVersion": "2022.3",
                            "downloads": {
                                "windows": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.1.exe",
                                    "size": 700077360,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.1.exe.sha256"
                                },
                                "linux": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.1.tar.gz",
                                    "size": 846981199,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.1.tar.gz.sha256"
                                }
                            },
                            "notesLink": "https://youtrack.jetbrains.com/articles/CPP-A-230654194",
                            "licenseRequired": true,
                            "build": "223.8214.51"
                        },
                        {
                            "date": "2022-11-30",
                            "type": "release",
                            "version": "2022.3",
                            "majorVersion": "2022.3",
                            "downloads": {
                                "windows": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.exe",
                                    "size": 668489488,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.exe.sha256"
                                },
                                "linux": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.3.tar.gz",
                                    "size": 835387407,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.3.tar.gz.sha256"
                                }
                            },
                            "licenseRequired": true,
                            "build": "223.7571.171"
                        }
                    ],
                    "forSale": true,
                    "productFamilyName": "CLion",
                    "intellijProductCode": "CL",
                    "alternativeCodes": [
                        "CL"
                    ],
                    "salesCode": "CL",
                    "link": "https://www.jetbrains.com/clion",
                    "description": "A cross-platform C and C++ IDE",
                    "types": [
                        {
                            "id": "code",
                            "name": "Code"
                        }
                    ],
                    "categories": [
                        "IDE"
                    ]
                },
                {
                    "code": "DS",
                    "name": "DataSpell",
                    "releases": [
                        {
                            "date": "2022-12-22",
                            "type": "release",
                            "version": "2022.3.1",
                            "majorVersion": "2022.3",
                            "downloads": {
                                "windows": {
                                    "link": "https://download.jetbrains.com/python/dataspell-2022.3.1.exe",
                                    "size": 647998336,
                                    "checksumLink": "https://download.jetbrains.com/python/dataspell-2022.3.1.exe.sha256"
                                },
                                "linux": {
                                    "link": "https://download.jetbrains.com/python/dataspell-2022.3.1.tar.gz",
                                    "size": 861638824,
                                    "checksumLink": "https://download.jetbrains.com/python/dataspell-2022.3.1.tar.gz.sha256"
                                }
                            },
                            "licenseRequired": true,
                            "build": "223.8214.61"
                        },
                        {
                            "date": "2022-12-01",
                            "type": "release",
                            "version": "2022.3",
                            "majorVersion": "2022.3",
                            "downloads": {
                                "windows": {
                                    "link": "https://download.jetbrains.com/python/dataspell-2022.3.exe",
                                    "size": 639166120,
                                    "checksumLink": "https://download.jetbrains.com/python/dataspell-2022.3.exe.sha256"
                                },
                                "linux": {
                                    "link": "https://download.jetbrains.com/python/dataspell-2022.3.tar.gz",
                                    "size": 850206395,
                                    "checksumLink": "https://download.jetbrains.com/python/dataspell-2022.3.tar.gz.sha256"
                                }
                            },
                            "licenseRequired": true,
                            "build": "223.7571.211"
                        }
                    ],
                    "forSale": true,
                    "productFamilyName": "DataSpell",
                    "intellijProductCode": "DS",
                    "alternativeCodes": [
                        "PD",
                        "DS"
                    ],
                    "salesCode": "DS",
                    "link": "https://www.jetbrains.com/dataspell",
                    "description": "An IDE for data scientists",
                    "categories": [
                        "IDE"
                    ]
                }
            ]
        """.trimIndent()

@Language("XML")
private val twoProductsWithSimpleCodesXml =
    """
            <products>
                <product name="CLion">
                    <code>CL</code>
                    <channel id="CL-RELEASE-licensing-RELEASE" name="CLion RELEASE" status="release"
                             url="https://www.jetbrains.com/clion/download" feedback="https://youtrack.jetbrains.com"
                             majorVersion="2022" licensing="release">
                        <build number="223.8214" version="2022.3.1" releaseDate="20221130" fullNumber="223.8214.51">
                            <blogPost
                                    url="https://blog.jetbrains.com/clion/2022/12/looking-forward-towards-2023-in-clion" />
                            <message>bla</message>
                            <button name="Download" url="https://www.jetbrains.com/clion/download" download="true" />
                            <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/CPP-A-230654194" />
                            <button name="More Information"
                                    url="https://blog.jetbrains.com/clion/2022/12/looking-forward-towards-2023-in-clion" />
                            <patch from="223.7571" size="from 243 to 384" fullFrom="223.7571.171" />
                        </build>
                        <build number="223.7571" version="2022.3" releaseDate="20221130" fullNumber="223.7571.171">
                            <blogPost url="https://www.jetbrains.com/clion/whatsnew/" />
                            <patch from="223.7571" size="from 215 to 343" fullFrom="223.7571.113" />
                            <patch from="222.4345" size="from 545 to 681" fullFrom="222.4345.21" />
                        </build>
                    </channel>
                </product>
            
                <product name="DataSpell">
                    <code>DS</code>
                    <code>PD</code>
                    <channel id="DS-RELEASE-licensing-RELEASE" name="DataSpell RELEASE" status="release"
                             url="https://www.jetbrains.com/dataspell" feedback="https://youtrack.jetbrains.com"
                             majorVersion="2022" licensing="release">
                        <build number="223.8214" version="2022.3.1" releaseDate="20221201" fullNumber="223.8214.61">
                            <blogPost url="https://blog.jetbrains.com/dataspell/2022/12/2022-3-1/" />
                            <message>blabla</message>
                            <button name="Download" url="https://www.jetbrains.com/dataspell" download="true" />
                            <button name="More Information" url="https://blog.jetbrains.com/dataspell/2022/12/2022-3-1/" />
                            <patch from="222.4459" size="from 610 to 642" fullFrom="222.4459.25" />
                            <patch from="223.8214" size="from 308 to 309" fullFrom="223.8214.28" />
                            <patch from="223.7571" size="from 334 to 341" fullFrom="223.7571.211" />
                        </build>
                        <build number="223.7571" version="2022.3" releaseDate="20221201" fullNumber="223.7571.211">
                            <blogPost url="https://blog.jetbrains.com/dataspell/2022/12/2022-3/" />
                            <patch from="222.4459" size="from 592 to 617" fullFrom="222.4459.25" />
                            <patch from="223.7571" size="from 310 to 311" fullFrom="223.7571.76" />
                        </build>
                    </channel>
                </product>
            </products>
        """.trimIndent()


