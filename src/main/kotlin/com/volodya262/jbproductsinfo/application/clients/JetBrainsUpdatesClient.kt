package com.volodya262.jbproductsinfo.application.clients

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.volodya262.jbproductsinfo.domain.BuildInfo
import com.volodya262.jbproductsinfo.domain.FamilyGroupBuilds
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class JetBrainsUpdatesClient(
    val jetBrainsUpdatesRestTemplate: RestTemplate,
    @Value("\${client.jetbrains-updates-url}")
    val baseUrl: String
) {
    fun getBuilds(buildReleasedAfter: LocalDate?): List<FamilyGroupBuilds> {
        val url = "$baseUrl/updates.xml"
        val productsXml = jetBrainsUpdatesRestTemplate.getForObject<ProductsXml>(url)

        val productCodeToBuildsMap = productsXml.products
            .map { productXml ->
                val productBuilds = productXml
                    .toBuildInfoTemps(channelFilterPredicate = { channel -> channel.status == "release" })
                    .filter { it.allRequiredFieldsFilled }
                    .filter { buildReleasedAfter == null || it.releaseDate!!.isAfter(buildReleasedAfter) }
                    .map {
                        BuildInfo(
                            productCode = it.productCode!!,
                            buildReleaseDate = it.releaseDate!!,
                            buildVersion = it.buildVersion!!,
                            buildFullNumber = it.buildFullNumber!!
                        )
                    }

                return@map FamilyGroupBuilds(
                    relatedProductCodes = productXml.codes.toSet(),
                    familyGroupName = productXml.name,
                    builds = productBuilds
                )
            }

        return productCodeToBuildsMap
    }

//    fun getProducts(): List<BuildGroupProduct> {
//        val url = "$baseUrl/updates.xml"
//        val productsXml = jetBrainsUpdatesRestTemplate.getForObject<ProductsXml>(url)
//        return productsXml.products.map { BuildGroupProduct(relatedProductCodes = it.codes.toSet(), productName = it.name) }
//    }
}

@JacksonXmlRootElement(localName = "products")
class ProductsXml {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "product")
    lateinit var products: List<ProductXml>
}

class ProductXml {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "code")
    lateinit var codes: List<String>

    @JacksonXmlProperty(isAttribute = true)
    lateinit var name: String

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "channel")
    lateinit var channels: List<ProductChannelXml>

    fun toBuildInfoTemps(channelFilterPredicate: (ProductChannelXml) -> Boolean): List<BuildInfoTemp> =
        channels.filter { channelFilterPredicate(it) }
            .flatMap { channel -> channel.toBuildInfoTemps().map { it.apply { productCode = codes.first() } } }
}

class ProductChannelXml {
    @JacksonXmlProperty(isAttribute = true)
    lateinit var id: String

    @JacksonXmlProperty(isAttribute = true)
    lateinit var status: String

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "build")
    lateinit var builds: List<ProductBuildXml>

    fun toBuildInfoTemps(): List<BuildInfoTemp> =
        builds.map { build -> build.toBuildInfoTemp().apply { channelId = id; channelStatus = status } }
}

class ProductBuildXml {
    @JacksonXmlProperty(isAttribute = true)
    var fullNumber: String? = null

    @JacksonXmlProperty(isAttribute = true)
    lateinit var number: String

    @JacksonXmlProperty(isAttribute = true)
    lateinit var version: String

    @JacksonXmlProperty(isAttribute = true)
    var releaseDate: String? = null

    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    fun toBuildInfoTemp(): BuildInfoTemp {
        val releaseDateParsed = releaseDate?.let { LocalDate.parse(it, formatter) }

        return BuildInfoTemp().apply {
            buildFullNumber = fullNumber ?: number
            buildVersion = version
            releaseDate = releaseDateParsed
        }
    }
}

class BuildInfoTemp {
    var productCode: String? = null
    var channelId: String? = null
    var channelStatus: String? = null
    var buildVersion: String? = null
    var buildFullNumber: String? = null
    var releaseDate: LocalDate? = null

    val allRequiredFieldsFilled: Boolean
        get() = productCode != null && channelId != null && channelStatus != null &&
            buildVersion != null && buildFullNumber != null && releaseDate != null
}
