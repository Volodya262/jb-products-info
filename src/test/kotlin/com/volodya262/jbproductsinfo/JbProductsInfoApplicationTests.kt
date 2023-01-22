package com.volodya262.jbproductsinfo

import com.volodya262.jbproductsinfo.application.clients.JetBrainsDataServicesClient
import com.volodya262.jbproductsinfo.application.clients.JetBrainsUpdatesClient
import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.repository.JdbcProductsRepository
import com.volodya262.jbproductsinfo.application.services.BuildQueueService
import com.volodya262.jbproductsinfo.application.services.RemoteBuildsInfoProviderService
import com.volodya262.libraries.testextensions.stubForJsonGet
import com.volodya262.libraries.testextensions.stubForXmlGet
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.servlet.MockMvc


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock
class JbProductsInfoApplicationTests(
    @Autowired val jetBrainsUpdatesClient: JetBrainsUpdatesClient,
    @Autowired val jetBrainsDataServicesClient: JetBrainsDataServicesClient,
    @Autowired val remoteBuildsInfoProviderService: RemoteBuildsInfoProviderService,
    @Autowired val buildQueueService: BuildQueueService,
    @Autowired val mockMvc: MockMvc,
    @Autowired val jdbcBuildsRepository: JdbcBuildsRepository,
    @Autowired val jdbcProductsRepository: JdbcProductsRepository
) {

    @BeforeEach
    fun beforeEach() {
        jdbcBuildsRepository.deleteAll()
        jdbcProductsRepository.deleteAll()
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun foo() {
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            xml2
        }

        stubForJsonGet("/data-services/products") {
            json2
        }

        buildQueueService.checkAndQueueBuilds()
    }

    @Language("JSON")
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

    @Language("XML")
    val simpleXml =
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
                        <build number="222.4345" version="2022.2.4" releaseDate="20220727" fullNumber="222.4345.21">
                            <blogPost url="https://blog.jetbrains.com/clion/2022/10/clion-bug-fix-update-2022-2-4" />
                            <message>bla</message>
                            <button name="Download"
                                    url="https://www.jetbrains.com/clion/download/previous.html#version20222" download="true" />
                            <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/CPP-A-230654181" />
                            <button name="More Information"
                                    url="https://blog.jetbrains.com/clion/2022/10/clion-bug-fix-update-2022-2-4" />
                            <patch from="222.4167" size="from 49 to 64" fullFrom="222.4167.35" />
                        </build>
                    </channel>
                    <channel id="CL-EAP-licensing-RELEASE" name="CLion EAP licensing:RELEASE" status="eap"
                             url="https://www.jetbrains.com/clion/nextversion" feedback="https://youtrack.jetbrains.com"
                             majorVersion="2022" licensing="release">
                        <build number="223.7571" version="2022.3 Release Candidate" fullNumber="223.7571.113">
                            <blogPost url="https://blog.jetbrains.com/clion/2022/11/clion-2022-3-release-candidate" />
                            <patch from="223.7571" size="from 216 to 344" fullFrom="223.7571.59" />
                        </build>
                        <build number="222.3345" version="2022.2 Release Candidate" fullNumber="222.3345.93">
                            <blogPost url="https://blog.jetbrains.com/clion/2022/07/clion-2022-2-release-candidate" />
                            <patch from="222.3345" size="from 166 to 262" fullFrom="222.3345.40" />
                        </build>
                        <build number="213.5744" version="2021.3 Release Candidate" fullNumber="213.5744.190">
                            <blogPost url="https://blog.jetbrains.com/clion/2021/11/clion-2021-3-release-candidate-2" />
                            <patch from="213.5744" size="from 57 to 59" fullFrom="213.5744.123" />
                        </build>
                    </channel>
                    <channel id="clion_12" name="CLion 1.2" status="release"
                             url="https://www.jetbrains.com/clion/download" feedback="https://youtrack.jetbrains.com"
                             majorVersion="1" licensing="release">
                        <build number="143.2370" version="1.2.5" releaseDate="20151102">
                            <message>bla</message>
                            <button name="Download"
                                    url="https://www.jetbrains.com/clion/download/previous.html#version12" download="true" />
                            <button name="Release Notes"
                                    url="https://blog.jetbrains.com/blog/2016/05/11/security-update-for-intellij-based-ides-v2016-1-and-older-versions/" />
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
                    <channel id="DS-EAP-licensing-EAP" name="DataSpell EAP" status="eap"
                             url="https://www.jetbrains.com/dataspell/nextversion/"
                             feedback="https://youtrack.jetbrains.com" majorVersion="2022" licensing="eap">
                        <build number="223.7571" version="2022.3 EAP" fullNumber="223.7571.17">
                            <blogPost url="https://blog.jetbrains.com/dataspell/2022/11/dataspell-2022-3-eap-2-is-out/" />
                            <patch from="222.4345" size="from 638 to 666" fullFrom="222.4345.24" />
                            <patch from="223.6160" size="from 370 to 374" fullFrom="223.6160.11" />
                        </build>
                    </channel>
                </product>
            </products>
        """.trimIndent()

    @Language("JSON")
    val json2 = """
        [
          {
            "code": "IIC",
            "name": "IntelliJ IDEA Community Edition",
            "releases": [
              {
                "date": "2022-12-20",
                "type": "release",
                "version": "2022.3.1",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-2022.3.1.exe",
                    "size": 681138616,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-2022.3.1.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-2022.3.1.tar.gz",
                    "size": 941758149,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-2022.3.1.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/IDEA-A-2100661418/IntelliJ-IDEA-2022.3.1-223.8214.52-build-Release-Notes",
                "licenseRequired": true,
                "build": "223.8214.52"
              },
              {
                "date": "2022-11-30",
                "type": "release",
                "version": "2022.3",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-2022.3.exe",
                    "size": 683003488,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-2022.3.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-2022.3.tar.gz",
                    "size": 947236841,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-2022.3.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/IDEA-A-2100661406/IntelliJ-IDEA-2022.3-223.7571.182-build-Release-Notes",
                "licenseRequired": true,
                "build": "223.7571.182"
              },
              {
                "date": "2022-11-23",
                "type": "release",
                "version": "2022.2.4",
                "majorVersion": "2022.2",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-2022.2.4.exe",
                    "size": 628566784,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-2022.2.4.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-2022.2.4.tar.gz",
                    "size": 862568767,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-2022.2.4.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/IDEA-A-2100661403/IntelliJ-IDEA-2022.2.4-222.4459.24-build-Release-Notes",
                "licenseRequired": true,
                "build": "222.4459.24"
              },
              {
                "date": "2015-11-02",
                "type": "release",
                "version": "15.0.6",
                "majorVersion": "15.0",
                "patches": {},
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-15.0.6.exe",
                    "size": 254961168,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-15.0.6.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIC-15.0.6.tar.gz",
                    "size": 234630621,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIC-15.0.6.tar.gz.sha256"
                  }
                },
                "notesLink": "https://confluence.jetbrains.com/display/IDEADEV/IntelliJ+IDEA+2016.1.3+Release+Notes",
                "licenseRequired": true,
                "build": "143.2370.31"
              }
            ],
            "forSale": false,
            "productFamilyName": "IntelliJ IDEA",
            "intellijProductCode": "IC",
            "alternativeCodes": [
              "IC"
            ],
            "link": "https://www.jetbrains.com/idea",
            "description": "The IDE for pure Java and Kotlin development",
            "categories": [
              "IDE"
            ]
          },
          {
            "code": "IIU",
            "name": "IntelliJ IDEA Ultimate",
            "releases": [
              {
                "date": "2022-12-20",
                "type": "release",
                "version": "2022.3.1",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-2022.3.1.exe",
                    "size": 791547136,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-2022.3.1.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-2022.3.1.tar.gz",
                    "size": 1115457871,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-2022.3.1.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/IDEA-A-2100661418/IntelliJ-IDEA-2022.3.1-223.8214.52-build-Release-Notes",
                "licenseRequired": true,
                "build": "223.8214.52"
              },
              {
                "date": "2022-11-30",
                "type": "release",
                "version": "2022.3",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-2022.3.exe",
                    "size": 792833080,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-2022.3.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-2022.3.tar.gz",
                    "size": 1119946465,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-2022.3.tar.gz.sha256"
                  }
                },
                "licenseRequired": true,
                "build": "223.7571.182"
              },
              {
                "date": "2022-11-23",
                "type": "release",
                "version": "2022.2.4",
                "majorVersion": "2022.2",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-2022.2.4.exe",
                    "size": 737577128,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-2022.2.4.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-2022.2.4.tar.gz",
                    "size": 1039070608,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-2022.2.4.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/IDEA-A-2100661403/IntelliJ-IDEA-2022.2.4-222.4459.24-build-Release-Notes",
                "licenseRequired": true,
                "build": "222.4459.24"
              },
              {
                "date": "2015-11-02",
                "type": "release",
                "version": "15.0.6",
                "majorVersion": "15.0",
                "patches": {},
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-15.0.6.exe",
                    "size": 254961168,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-15.0.6.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/idea/ideaIU-15.0.6.tar.gz",
                    "size": 234630621,
                    "checksumLink": "https://download.jetbrains.com/idea/ideaIU-15.0.6.tar.gz.sha256"
                  }
                },
                "notesLink": "https://confluence.jetbrains.com/display/IDEADEV/IntelliJ+IDEA+2016.1.3+Release+Notes",
                "licenseRequired": true,
                "build": "143.2370.31"
              }
            ],
            "forSale": true,
            "productFamilyName": "IntelliJ IDEA",
            "intellijProductCode": "IU",
            "alternativeCodes": [
              "IU"
            ],
            "salesCode": "II",
            "link": "https://www.jetbrains.com/idea",
            "description": "The Leading Java and Kotlin IDE"
          },
          {
            "code": "PCC",
            "name": "PyCharm Community Edition",
            "releases": [
              {
                "date": "2022-12-28",
                "type": "release",
                "version": "2022.3.1",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/python/pycharm-community-2022.3.1.exe",
                    "size": 422493552,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-community-2022.3.1.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/python/pycharm-community-2022.3.1.tar.gz",
                    "size": 574512592,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-community-2022.3.1.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/PY-A-233538034/PyCharm-2022.3.1-223.8214.51-build-Release-Notes",
                "licenseRequired": true,
                "build": "223.8214.51"
              },
              {
                "date": "2022-12-01",
                "type": "release",
                "version": "2022.3",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/python/pycharm-community-2022.3.exe",
                    "size": 414052512,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-community-2022.3.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/python/pycharm-community-2022.3.tar.gz",
                    "size": 562364769,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-community-2022.3.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/PY-A-233538031/PyCharm-2022.3-223.7571.203-build-Release-Notes",
                "licenseRequired": true,
                "build": "223.7571.203"
              },
              {
                "date": "2022-11-17",
                "type": "release",
                "version": "2022.2.4",
                "majorVersion": "2022.2",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/python/pycharm-community-2022.2.4.exe",
                    "size": 397125960,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-community-2022.2.4.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/python/pycharm-community-2022.2.4.tar.gz",
                    "size": 535908869,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-community-2022.2.4.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/PY-A-233538026/PyCharm-2022.2.4-222.4459.20-build-Release-Notes",
                "licenseRequired": true,
                "build": "222.4459.20"
              }
            ],
            "forSale": false,
            "productFamilyName": "PyCharm",
            "intellijProductCode": "PC",
            "alternativeCodes": [
              "PCA",
              "PC"
            ],
            "link": "https://www.jetbrains.com/pycharm",
            "description": "The pure Python IDE"
          },
          {
            "code": "PCP",
            "name": "PyCharm Professional Edition",
            "releases": [
              {
                "date": "2022-12-28",
                "type": "release",
                "version": "2022.3.1",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/python/pycharm-professional-2022.3.1.exe",
                    "size": 513169376,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-professional-2022.3.1.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/python/pycharm-professional-2022.3.1.tar.gz",
                    "size": 720880776,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-professional-2022.3.1.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/PY-A-233538034/PyCharm-2022.3.1-223.8214.51-build-Release-Notes",
                "licenseRequired": true,
                "build": "223.8214.51"
              },
              {
                "date": "2022-12-01",
                "type": "release",
                "version": "2022.3",
                "majorVersion": "2022.3",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/python/pycharm-professional-2022.3.exe",
                    "size": 504214264,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-professional-2022.3.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/python/pycharm-professional-2022.3.tar.gz",
                    "size": 707829681,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-professional-2022.3.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/PY-A-233538031/PyCharm-2022.3-223.7571.203-build-Release-Notes",
                "licenseRequired": true,
                "build": "223.7571.203"
              },
              {
                "date": "2022-11-17",
                "type": "release",
                "version": "2022.2.4",
                "majorVersion": "2022.2",
                "downloads": {
                  "windows": {
                    "link": "https://download.jetbrains.com/python/pycharm-professional-2022.2.4.exe",
                    "size": 493255624,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-professional-2022.2.4.exe.sha256"
                  },
                  "linux": {
                    "link": "https://download.jetbrains.com/python/pycharm-professional-2022.2.4.tar.gz",
                    "size": 697025844,
                    "checksumLink": "https://download.jetbrains.com/python/pycharm-professional-2022.2.4.tar.gz.sha256"
                  }
                },
                "notesLink": "https://youtrack.jetbrains.com/articles/PY-A-233538026/PyCharm-2022.2.4-222.4459.20-build-Release-Notes",
                "licenseRequired": true,
                "build": "222.4459.20"
              }
            ],
            "forSale": true,
            "productFamilyName": "PyCharm",
            "intellijProductCode": "PY",
            "alternativeCodes": [
              "PYA",
              "PY"
            ],
            "salesCode": "PC",
            "link": "https://www.jetbrains.com/pycharm",
            "description": "The full-stack Python IDE"
          }
        ]
    """.trimIndent()

    val xml2 = """
        <products>
          <product name="IntelliJ IDEA">
            <code>IC</code>
            <code>IU</code>
            <channel id="IC-IU-RELEASE-licensing-RELEASE" name="IntelliJ IDEA RELEASE" status="release" url="https://www.jetbrains.com/idea/download" feedback="https://youtrack.jetbrains.com/issues/IDEA" majorVersion="2022" licensing="release">
              <build number="223.8214" version="2022.3.1" releaseDate="20221130" fullNumber="223.8214.52">
                <blogPost url="https://blog.jetbrains.com/idea/2022/12/intellij-idea-2022-3-1/"/>
                <message>bla</message>
                <button name="Download" url="https://www.jetbrains.com/idea/download" download="true"/>
                <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/IDEA-A-2100661418/IntelliJ-IDEA-2022.3.1-223.8214.52-build-Release-Notes"/>
                <button name="More Information" url="https://blog.jetbrains.com/idea/2022/12/intellij-idea-2022-3-1/"/>
                <patch from="223.8214" size="from 32 to 43" fullFrom="223.8214.27"/>
                <patch from="223.7571" size="from 51 to 70" fullFrom="223.7571.182"/>
                <patch from="222.4459" size="from 556 to 690" fullFrom="222.4459.24"/>
              </build>
              <build number="223.7571" version="2022.3" releaseDate="20221130" fullNumber="223.7571.182">
                <blogPost url="https://www.jetbrains.com/idea/whatsnew/"/>
                <patch from="223.7571" size="from 32 to 42" fullFrom="223.7571.123"/>
                <patch from="222.4459" size="from 569 to 708" fullFrom="222.4459.24"/>
              </build>
              <build number="222.4459" version="2022.2.4" releaseDate="20220726" fullNumber="222.4459.24">
                <blogPost url="https://blog.jetbrains.com/idea/2022/11/intellij-idea-2022-2-4/"/>
                <message>bla</message>
                <button name="Download" url="https://www.jetbrains.com/idea/download/previous.html#version20222" download="true"/>
                <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/IDEA-A-2100661403/IntelliJ-IDEA-2022.2.4-222.4459.24-build-Release-Notes"/>
                <button name="More Information" url="https://blog.jetbrains.com/idea/2022/11/intellij-idea-2022-2-4/"/>
                <patch from="222.4345" size="from 35 to 45" fullFrom="222.4345.14"/>
                <patch from="221.6008" size="from 321 to 379" fullFrom="221.6008.13"/>
                <patch from="222.4459" size="from 24 to 34" fullFrom="222.4459.16"/>
              </build>
            </channel>
            <channel id="IDEA15_Release" name="IntelliJ IDEA Release" status="release" url="https://www.jetbrains.com/idea/download" feedback="https://youtrack.jetbrains.com/issues/IDEA" majorVersion="15" licensing="release">
              <build number="143.2370" version="15.0.6" releaseDate="20151102" fullNumber="143.2370.31">
                <message><![CDATA[<p>A new IntelliJ IDEA 2016.3.8 bug-fix update is here! The update brings an important fix to the built-in<br> SSH client and makes it compatible with the new GitHub cryptographic standards.</p>]]></message>
                <button name="Download" url="https://www.jetbrains.com/idea/download/previous.html#version150" download="true"/>
                <button name="Release Notes" url="https://confluence.jetbrains.com/display/IDEADEV/IntelliJ+IDEA+2016.1.3+Release+Notes"/>
              </build>
            </channel>
          </product>
          <product name="PyCharm">
            <code>PC</code>
            <code>PCA</code>
            <code>PY</code>
            <code>PYA</code>
            <channel id="PC-PY-RELEASE-licensing-RELEASE" name="PyCharm RELEASE" status="release" url="https://www.jetbrains.com/pycharm/download" feedback="https://youtrack.jetbrains.com" majorVersion="2022" licensing="release">
              <build number="223.8214" version="2022.3.1" releaseDate="20221201" fullNumber="223.8214.51">
                <message>bla</message>
                <button name="Download" url="https://www.jetbrains.com/pycharm/download" download="true"/>
                <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/PY-A-233538034/PyCharm-2022.3.1-223.8214.51-build-Release-Notes"/>
                <patch from="223.8214" size="from 72 to 88" fullFrom="223.8214.17"/>
                <patch from="222.4459" size="from 340 to 452" fullFrom="222.4459.20"/>
              </build>
              <build number="223.7571" version="2022.3" releaseDate="20221201" fullNumber="223.7571.203">
                <blogPost url="https://blog.jetbrains.com/pycharm/2022/12/2022-3/"/>
                <patch from="223.7571" size="from 71 to 81" fullFrom="223.7571.175"/>
                <patch from="222.4459" size="from 321 to 425" fullFrom="222.4459.20"/>
              </build>
              <build number="222.4459" version="2022.2.4" releaseDate="20220728" fullNumber="222.4459.20">
                <message>ddd</message>
                <button name="Download" url="https://www.jetbrains.com/pycharm/download/previous.html#version20222" download="true"/>
                <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/PY-A-233538026/PyCharm-2022.2.4-222.4459.20-build-Release-Notes"/>
                <patch from="222.4459" size="from 64 to 74" fullFrom="222.4459.4"/>
                <patch from="221.6008" size="from 236 to 286" fullFrom="221.6008.17"/>
              </build>
            </channel>
          </product>
        </products>
    """.trimIndent()
}
