package com.volodya262.jbproductsinfo.application.clients

import com.volodya262.jbproductsinfo.domain.BuildDownloadInfo
import com.volodya262.jbproductsinfo.domain.Product
import com.volodya262.jbproductsinfo.domain.ProductAndBuildDownloadInfos
import com.volodya262.jbproductsinfo.domain.ProductCode
import com.volodya262.jbproductsinfo.libraries.resttemplateextensions.getForObjectReified
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class JetBrainsDataServicesClient(
    private val jetBrainsDataServicesRestTemplate: RestTemplate,
    @Value("\${client.jetbrains-data-services-url}")
    val baseUrl: String
) {
    fun getProductDownloadsInfos(): List<ProductAndBuildDownloadInfos> {
        val url = "$baseUrl/products"
        val productInfoDtos = jetBrainsDataServicesRestTemplate.getForObjectReified<List<ProductInfoDto>>(url)!!

        val productCodeToBuildDownloadInfosMap = productInfoDtos
            .map { productInfo ->
                val buildDownloadInfos = productInfo.releases
                    .map { release ->
                        BuildDownloadInfo(
                            productCode = productInfo.resolvedProductCode,
                            buildReleaseDate = release.dateAsLocalDate,
                            buildVersion = release.version,
                            buildDownloadUrl = release.downloads?.linux?.link,
                            buildFullNumber = release.build
                        )
                    }

                return@map ProductAndBuildDownloadInfos(productInfo.toProduct(), buildDownloadInfos)
            }

        return productCodeToBuildDownloadInfosMap
    }

    fun getProducts(): List<Product> {
        val url = "$baseUrl/products"
        val productInfoDtos = jetBrainsDataServicesRestTemplate.getForObjectReified<List<ProductInfoDto>>(url)!!

        return productInfoDtos.map {
            Product(
                productCode = it.resolvedProductCode,
                productName = it.name,
                alternativeCodes = it.resolvedAlternativeProductCodes
            )
        }
    }
}

class ProductInfoDto(
    private val intellijProductCode: String,
    private val code: String,
    private val alternativeCodes: List<String>?,
    val name: String,
    val releases: List<ProductInfoReleaseDto>
) {
    val resolvedProductCode: ProductCode =
        intellijProductCode
    val resolvedAlternativeProductCodes: Set<ProductCode> =
        ((alternativeCodes ?: emptyList()) + listOf(code)).toSet().minus(intellijProductCode)
    fun toProduct() =
        Product(
            productCode = resolvedProductCode,
            productName = name,
            alternativeCodes = resolvedAlternativeProductCodes
        )
}

class ProductInfoReleaseDto(
    val date: String,
    val version: String,
    val build: String, // build full number, i.e. 222.111.333
    val downloads: ProductInfoReleaseDownloadsDto?
) {
    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    val dateAsLocalDate: LocalDate
        get() = LocalDate.parse(date, formatter)
}

class ProductInfoReleaseDownloadsDto(
    val linux: ProductInfoReleaseDownloadItemDto?
)

class ProductInfoReleaseDownloadItemDto(
    val link: String
)