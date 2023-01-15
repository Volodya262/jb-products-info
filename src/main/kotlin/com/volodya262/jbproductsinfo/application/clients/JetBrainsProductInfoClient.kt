package com.volodya262.jbproductsinfo.application.clients

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class JetBrainsProductInfoClient(
    val xmlProductInfoRestTemplate: RestTemplate
) {
    fun getProductInfo() {
        val resp = xmlProductInfoRestTemplate.getForObject<ProductsXml>("https://www.jetbrains.com/updates/updates.xml")

        val res = resp.products.flatMap { it.toBuildInfoTemps() }
        println(res)

        // TODO figure out the domain model
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

    fun toBuildInfoTemps(): List<BuildInfoTemp> =
        channels
            .flatMap { channel -> channel.toBuildInfoTemps().map { it.apply { productCode = code } } }
            .groupBy { it.buildNumber!! }
            .values
            .reduce { acc, buildInfoTemps -> acc + buildInfoTemps.mergeInfo()}
}

fun List<BuildInfoTemp>.mergeInfo(): BuildInfoTemp {
    val releaseDate = this.fold(null) { acc: LocalDate?, buildInfoTemp: BuildInfoTemp ->
        if (buildInfoTemp.releaseDate != null) buildInfoTemp.releaseDate else acc
    }

    if (releaseDate == null) {
        println("Failed to find release date")
    }

    val obj = this.first()
    return BuildInfoTemp().apply {
        productCode = obj.productCode
        buildNumber = obj.buildNumber
        this.releaseDate = releaseDate
    }
}

class ProductChannelXml {
    @JacksonXmlProperty(isAttribute = true)
    lateinit var id: String

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "build")
    lateinit var builds: List<ProductBuildXml>

    fun toBuildInfoTemps(): List<BuildInfoTemp> = builds.map { it.toBuildInfoTemp() }
}

class ProductBuildXml {
    @JacksonXmlProperty(isAttribute = true)
    lateinit var number: String

    @JacksonXmlProperty(isAttribute = true)
    var releaseDate: String? = null

    fun toBuildInfoTemp(): BuildInfoTemp {
        val releaseDateParsed = releaseDate?.let { LocalDate.parse(it, formatter) }

        return BuildInfoTemp().apply {
            buildNumber = number
            releaseDate = releaseDateParsed
        }
    }

    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
}

class BuildInfoTemp {
    var productCode: String? = null
//    var channelId: String? = null
    var buildNumber: String? = null
    var releaseDate: LocalDate? = null
}