package com.volodya262.jbproductsinfo.application.clients

import com.volodya262.jbproductsinfo.domain.BuildDownloadInfo
import com.volodya262.jbproductsinfo.domain.ProductCode
import com.volodya262.jbproductsinfo.libraries.resttemplateextensions.getForObjectReified
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class JetBrainsDataServicesClient(
    private val jetBrainsDataServicesRestTemplate: RestTemplate
) {
    fun getProductCodeToBuildDownloadInfosMap(after: LocalDate): Map<ProductCode, List<BuildDownloadInfo>> {
        val url = "https://data.services.jetbrains.com/products"
        val res = jetBrainsDataServicesRestTemplate.getForObjectReified<List<ProductInfoDto>>(url)!!

        val productCodeToBuildDownloadInfosMap = res
            .map { productInfo ->
                val buildDownloadInfos = productInfo.releases
                    .filter { release -> release.downloads?.linux?.link != null }
                    .filter { release -> release.dateAsLocalDate.isAfter(after) }
                    .map { release ->
                        BuildDownloadInfo(
                            productCode = productInfo.code,
                            releaseDate = release.dateAsLocalDate,
                            version = release.version,
                            downloadUrl = release.downloads!!.linux!!.link
                        )
                    }

                return@map productInfo.code to buildDownloadInfos
            }.toMap()

        return productCodeToBuildDownloadInfosMap
    }
}

class ProductInfoDto(
    val code: String, val name: String, val releases: List<ProductInfoReleaseDto>
)

class ProductInfoReleaseDto(
    val date: String,
    val version: String,
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