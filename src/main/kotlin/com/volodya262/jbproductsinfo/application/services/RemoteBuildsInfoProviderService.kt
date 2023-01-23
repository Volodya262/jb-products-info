package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.application.clients.JetBrainsDataServicesClient
import com.volodya262.jbproductsinfo.application.clients.JetBrainsUpdatesClient
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInfo
import com.volodya262.jbproductsinfo.domain.CurrentDateTimeProvider
import com.volodya262.jbproductsinfo.domain.MissingUrlReason
import com.volodya262.jbproductsinfo.domain.Product
import com.volodya262.jbproductsinfo.domain.ProductCode
import com.volodya262.jbproductsinfo.domain.ProductNotFound
import com.volodya262.jbproductsinfo.domain.ProductRelease
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Profile("default", "default_api", "test")
class RemoteBuildsInfoProviderService(
    val jetBrainsDataServicesClient: JetBrainsDataServicesClient,
    val jetBrainsUpdatesClient: JetBrainsUpdatesClient,
    val currentDateTimeProvider: CurrentDateTimeProvider
) {

    val logger: Logger = LoggerFactory.getLogger(RemoteBuildsInfoProviderService::class.java)

    fun getProductsBuilds(
        filterReleasedAfter: LocalDate,
        filterProductCode: ProductCode? = null
    ): Pair<List<Product>, Map<ProductCode, List<BuildInProcess>>> {
        val allProductsWithDownloadInfo =
            jetBrainsDataServicesClient.getProductDownloadsInfos(filterReleasedAfter, filterProductCode)
        val familyGroupBuildsList = jetBrainsUpdatesClient.getBuilds(filterReleasedAfter)

        val productAndAssociatedBuildsList = allProductsWithDownloadInfo
            .map { productWithDownloadInfos ->
                Pair(
                    productWithDownloadInfos,
                    familyGroupBuildsList.find { it.relatedProductCodes.contains(productWithDownloadInfos.product.productCode) }
                )
            }
            .filter { it.second != null }

        val products = productAndAssociatedBuildsList.map { it.first.product }

        if (filterProductCode != null && products.isEmpty()) {
            throw ProductNotFound(filterProductCode)
        }

        val buildsToProcess = productAndAssociatedBuildsList
            .map { (productAndBuildDownloadInfos, familyGroupBuilds) ->
                val productCode = productAndBuildDownloadInfos.product.productCode
                val buildsToProcess = merge(
                    familyGroupBuilds!!.builds,
                    productAndBuildDownloadInfos.productReleases,
                    productCode
                )

                logger.info("Found ${buildsToProcess.size} builds for product $productCode")
                return@map productCode to buildsToProcess
            }.toMap()

        return Pair(products, buildsToProcess)
    }

    private fun merge(
        familyGroupBuilds: List<BuildInfo>,
        productReleases: List<ProductRelease>,
        productCode: String
    ): List<BuildInProcess> {
        val buildFullNumberToDownloadInfo = productReleases.associateBy { it.buildFullNumber }

        return familyGroupBuilds.map {
            val downloadInfo = buildFullNumberToDownloadInfo[it.buildFullNumber]

            if (downloadInfo == null) {
                return@map BuildInProcess(productCode, it.buildFullNumber, currentDateTimeProvider)
                    .apply { toFailedToConstruct(MissingUrlReason.FailedToFindAssociatedVersion) }
            }

            if (downloadInfo.downloadUrl == null) {
                return@map BuildInProcess(productCode, it.buildFullNumber, currentDateTimeProvider)
                    .apply { toFailedToConstruct(MissingUrlReason.NoLinuxDistribution) }
            }

            return@map BuildInProcess(productCode, it.buildFullNumber, currentDateTimeProvider)
                .apply { toCreated(downloadInfo.downloadUrl, it.buildReleaseDate) }
        }
    }
}
