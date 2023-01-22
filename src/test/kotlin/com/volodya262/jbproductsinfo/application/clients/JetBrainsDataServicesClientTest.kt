package com.volodya262.jbproductsinfo.application.clients

import com.volodya262.libraries.testextensions.stubForJsonGet
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.arrayWithSize
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock

@SpringBootTest
@AutoConfigureWireMock
class JetBrainsDataServicesClientTest(
    @Autowired val jetBrainsDataServicesClient: JetBrainsDataServicesClient
) {
    @Test
    fun `should get product download infos`() {
        stubForJsonGet("/data-services/products") {
            simpleJson
        }

        val downloadInfos = jetBrainsDataServicesClient.getProductDownloadsInfos()

        val clionDownloadInfos = downloadInfos.find { it.product.productCode == "CL" }
        assertNotNull(clionDownloadInfos)
        assertThat(clionDownloadInfos!!.productReleases.toTypedArray(), arrayWithSize(5))

        val dataSpellDownloadInfos = downloadInfos.find { it.product.productCode == "DS" }
        assertNotNull(dataSpellDownloadInfos)
        assertThat(dataSpellDownloadInfos!!.productReleases.toTypedArray(), arrayWithSize(2))
    }

    val simpleJson =
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
                        },
                        {
                            "date": "2022-10-11",
                            "type": "release",
                            "version": "2022.2.4",
                            "majorVersion": "2022.2",
                            "downloads": {
                                "windows": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.2.4.exe",
                                    "size": 729033192,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.2.4.exe.sha256"
                                },
                                "linux": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.2.4.tar.gz",
                                    "size": 895935196,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.2.4.tar.gz.sha256"
                                }
                            },
                            "notesLink": "https://youtrack.jetbrains.com/articles/CPP-A-230654181",
                            "licenseRequired": true,
                            "build": "222.4345.21"
                        },
                        {
                            "date": "2022-09-15",
                            "type": "release",
                            "version": "2022.2.3",
                            "majorVersion": "2022.2",
                            "downloads": {
                                "windows": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.2.3.exe",
                                    "size": 729063552,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.2.3.exe.sha256"
                                },
                                "linux": {
                                    "link": "https://download.jetbrains.com/cpp/CLion-2022.2.3.tar.gz",
                                    "size": 895931413,
                                    "checksumLink": "https://download.jetbrains.com/cpp/CLion-2022.2.3.tar.gz.sha256"
                                }
                            },
                            "notesLink": "https://youtrack.jetbrains.com/articles/CPP-A-230654177",
                            "licenseRequired": true,
                            "build": "222.4167.35"
                        },
                        {
                            "date": "2016-05-11",
                            "type": "release",
                            "version": "1.2.5",
                            "majorVersion": "1.2",
                            "patches": {},
                            "downloads": {
                                "windows": {
                                    "link": "https://download.jetbrains.com/cpp/clion-1.2.5.exe",
                                    "size": 179594872,
                                    "checksumLink": "https://download.jetbrains.com/cpp/clion-1.2.5.exe.sha256"
                                },
                                "linux": {
                                    "link": "https://download.jetbrains.com/cpp/clion-1.2.5.tar.gz",
                                    "size": 228774487,
                                    "checksumLink": "https://download.jetbrains.com/cpp/clion-1.2.5.tar.gz.sha256"
                                }
                            },
                            "notesLink": "https://blog.jetbrains.com/blog/2016/05/11/security-update-for-intellij-based-ides-v2016-1-and-older-versions/",
                            "licenseRequired": true,
                            "build": "143.2370"
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
}