package com.volodya262.jbproductsinfo.application.clients

import com.volodya262.jbproductsinfo.domain.BuildInfo
import com.volodya262.libraries.testextensions.stubForXmlGet
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureWireMock
class JetBrainsUpdatesClientTest(
    @Autowired val jetBrainsUpdatesClient: JetBrainsUpdatesClient
) {

    @Test
    fun `should get products updates from release channel`() {
        // arrange
        stubForXmlGet("/jetbrains-updates/updates.xml") {
            simpleXml
        }

        // act
        val productCodeToBuildsMap = jetBrainsUpdatesClient.getBuilds(null)

        // assert
        val expectedClBuilds = listOf(
            BuildInfo(
                productCode = "CL",
                buildReleaseDate = createLocalDate("20221130"),
                buildVersion = "2022.3.1",
                buildFullNumber = "223.8214.51"
            ),
            BuildInfo(
                productCode = "CL",
                buildReleaseDate = createLocalDate("20221130"),
                buildVersion = "2022.3",
                buildFullNumber = "223.7571.171"
            ),
            BuildInfo(
                productCode = "CL",
                buildReleaseDate = createLocalDate("20220727"),
                buildVersion = "2022.2.4",
                buildFullNumber = "222.4345.21"
            ),
            BuildInfo(
                productCode = "CL",
                buildReleaseDate = createLocalDate("20151102"),
                buildVersion = "1.2.5",
                buildFullNumber = "143.2370"
            )
        )

        val expectedDsBuilds = listOf(
            BuildInfo(
                productCode = "DS",
                buildReleaseDate = createLocalDate("20221201"),
                buildVersion = "2022.3.1",
                buildFullNumber = "223.8214.61"
            ),
            BuildInfo(
                productCode = "DS",
                buildReleaseDate = createLocalDate("20221201"),
                buildVersion = "2022.3",
                buildFullNumber = "223.7571.211"
            )
        )

        val clionBuilds = productCodeToBuildsMap.find { it.relatedProductCodes.contains("CL") }
        assertThat(clionBuilds!!.builds, containsInAnyOrder(*expectedClBuilds.toTypedArray()))

        val dataSpellBuilds = productCodeToBuildsMap.find { it.relatedProductCodes.contains("DS") }
        assertThat(dataSpellBuilds!!.builds, containsInAnyOrder(*expectedDsBuilds.toTypedArray()))
    }

    private fun createLocalDate(s: String): LocalDate {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return LocalDate.parse(s, formatter)
    }

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
}