package com.volodya262.jbproductsinfo.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.volodya262.jbproductsinfo.application.clients.ProductsXml
import org.junit.jupiter.api.Test

class ProductsXmlTest {
    @Test
    fun `should deserialize xml to ProductsXml`() {
        val xmlMapper = XmlMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        val res = xmlMapper.readValue<ProductsXml>(xmlString)

        val buildInfos = res.products[0].toBuildInfoTemps { true }

        println(buildInfos)
    }
}

val xmlString = """
        <products>
            <product name="AppCode">
                <code>OC</code>
                <channel id="OC-RELEASE-licensing-RELEASE" name="AppCode RELEASE" status="release"
                         url="https://www.jetbrains.com/objc/download" feedback="https://youtrack.jetbrains.com"
                         majorVersion="2022" licensing="release">
                    <build number="223.8214" version="2022.3.1" releaseDate="20221214" fullNumber="223.8214.66">
                        <message>blabla</message>
                        <button name="Download" url="https://www.jetbrains.com/objc/download" download="true"/>
                        <button name="Release Notes" url="https://youtrack.jetbrains.com/articles/OC-A-223445230"/>
                        <patch from="223.8214" size="45" fullFrom="223.8214.62"/>
                        <patch from="222.4459" size="751" fullFrom="222.4459.24"/>
                        <patch from="223.7571" size="61" fullFrom="223.7571.236"/>
                    </build>
                    <build number="223.7571" version="2022.3" releaseDate="20221214" fullNumber="223.7571.236">
                        <patch from="222.4459" size="742" fullFrom="222.4459.24"/>
                        <patch from="223.7571" size="137" fullFrom="223.7571.233"/>
                    </build>
                    <build number="222.3345" version="2022.2" releaseDate="20220803" fullNumber="222.3345.144">
                        <patch from="221.6008" size="216" fullFrom="221.6008.18"/>
                        <patch from="222.3345" size="34" fullFrom="222.3345.115"/>
                    </build>
                </channel>
                <channel id="OC-EAP-licensing-RELEASE" name="AppCode EAP licensing:RELEASE" status="eap"
                         url="https://www.jetbrains.com/objc/nextversion" feedback="https://youtrack.jetbrains.com"
                         majorVersion="2022" licensing="release">
                    <build number="223.7571" version="2022.3 Release Candidate" fullNumber="223.7571.233">
                        <blogPost url="https://blog.jetbrains.com/appcode/2022/12/appcode-2022-3-release-candidate-is-here"/>
                        <patch from="223.7571" size="290" fullFrom="223.7571.113"/>
                    </build>
                    <build number="222.3345" version="2022.2 Release Candidate" fullNumber="222.3345.115">
                        <blogPost url="https://blog.jetbrains.com/appcode/2022/07/appcode-2022-2-release-candidate-is-here"/>
                        <patch from="222.3345" size="35" fullFrom="222.3345.83"/>
                    </build>
                </channel>
            </product>
        </products>
    """.trimIndent()