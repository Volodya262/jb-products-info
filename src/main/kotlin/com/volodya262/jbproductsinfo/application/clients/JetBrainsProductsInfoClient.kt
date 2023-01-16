package com.volodya262.jbproductsinfo.application.clients

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.volodya262.jbproductsinfo.domain.BuildInfo
import com.volodya262.jbproductsinfo.domain.Product
import com.volodya262.jbproductsinfo.domain.ProductsInfo
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class JetBrainsProductsInfoClient(
    val jetBrainsProductsInfoRestTemplate: RestTemplate
) {
    fun getProductsInfo(buildsAfter: LocalDate): ProductsInfo {
        val url = "https://www.jetbrains.com/updates/updates.xml"
        val productsXml = jetBrainsProductsInfoRestTemplate.getForObject<ProductsXml>(url)

        val products = productsXml.products.map { Product(productCode = it.code, productName = it.name) }

        val productCodeToBuildsMap = productsXml.products
            .map { productXml ->
                val productBuilds = productXml
                    .toBuildInfoTemps(channelFilterPredicate = { channel -> channel.status == "release" })
                    .filter { it.allRequiredFieldsFilled }
                    .filter { it.releaseDate!!.isAfter(buildsAfter) }
                    .map {
                        BuildInfo(
                            productCode = it.productCode!!,
                            releaseDate = it.releaseDate!!,
                            buildVersion = it.buildVersion!!,
                            buildNumber = it.buildNumber!!
                        )
                    }

                return@map productXml.code to productBuilds
            }.toMap()

        return ProductsInfo(products, productCodeToBuildsMap)
    }
}

@JacksonXmlRootElement(localName = "products")
class ProductsXml {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "product")
    lateinit var products: List<ProductXml>
}

class ProductXml {
    @JacksonXmlProperty(localName = "code")
    lateinit var code: String

    @JacksonXmlProperty(isAttribute = true)
    lateinit var name: String

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "channel")
    lateinit var channels: List<ProductChannelXml>

    fun toBuildInfoTemps(channelFilterPredicate: (ProductChannelXml) -> Boolean): List<BuildInfoTemp> =
        channels.filter { channelFilterPredicate(it) }
            .flatMap { channel -> channel.toBuildInfoTemps().map { it.apply { productCode = code } } }
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
            buildNumber = number
            buildVersion = version
            releaseDate = releaseDateParsed
        }
    }
}

class BuildInfoTemp {
    var productCode: String? = null
    var channelId: String? = null // TODO maybe delete it
    var channelStatus: String? = null
    var buildVersion: String? = null
    var buildNumber: String? = null
    var releaseDate: LocalDate? = null

    val allRequiredFieldsFilled: Boolean
        get() = productCode != null && channelId != null && channelStatus != null && buildVersion != null && buildNumber != null && releaseDate != null
}