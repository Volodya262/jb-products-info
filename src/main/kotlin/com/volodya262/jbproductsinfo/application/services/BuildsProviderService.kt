package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.application.clients.JetBrainsDataServicesClient
import com.volodya262.jbproductsinfo.application.clients.JetBrainsUpdatesClient
import com.volodya262.jbproductsinfo.domain.BuildDownloadInfo
import com.volodya262.jbproductsinfo.domain.BuildInfo
import com.volodya262.jbproductsinfo.domain.BuildToProcess
import com.volodya262.jbproductsinfo.domain.MissingUrlReason
import com.volodya262.jbproductsinfo.domain.Product
import com.volodya262.jbproductsinfo.domain.ProductCode
import org.springframework.stereotype.Component

@Component
class BuildsProviderService(
    val jetBrainsDataServicesClient: JetBrainsDataServicesClient,
    val jetBrainsUpdatesClient: JetBrainsUpdatesClient,
) {
    fun getProductsBuilds(): Pair<List<Product>, Map<ProductCode, List<BuildToProcess>>> {
        val allProductsWithDownloadInfo = jetBrainsDataServicesClient.getProductDownloadsInfos()
        val familyGroupBuildsList = jetBrainsUpdatesClient.getBuilds()

        val productAndAssociatedBuildsList = allProductsWithDownloadInfo
            .map { dataServicesProduct ->
                Pair(
                    dataServicesProduct,
                    familyGroupBuildsList.find { it.relatedProductCodes.contains(dataServicesProduct.product.productCode) }
                )
            }
            .filter { it.second != null }

        val products = productAndAssociatedBuildsList.map { it.first.product }

        val buildsToProcess = productAndAssociatedBuildsList.map { (productAndBuildDownloadInfos, familyGroupBuilds) ->
            val productCode = productAndBuildDownloadInfos.product.productCode
            val buildsToProcess = merge(
                familyGroupBuilds!!.builds,
                productAndBuildDownloadInfos.buildDownloadInfos,
                productCode
            )
            return@map productCode to buildsToProcess
        }.toMap()

        // TODO обработать ситуацию когда продукт если в списке билдов, но его нет в json

        return Pair(products, buildsToProcess)
    }

    fun merge(
        familyGroupBuilds: List<BuildInfo>,
        buildDownloadInfos: List<BuildDownloadInfo>,
        productCode: String
    ): List<BuildToProcess> {
        val buildFullNumberToDownloadInfo = buildDownloadInfos.associateBy { it.buildFullNumber }

        return familyGroupBuilds.map {
            val downloadInfo = buildFullNumberToDownloadInfo[it.buildFullNumber]

            if (downloadInfo == null) {
                return@map BuildToProcess(
                    productCode = productCode,
                    buildFullNumber = it.buildFullNumber,
                    missingUrlReason = MissingUrlReason.FailedToFindAssociatedVersion
                )
            }

            if (downloadInfo.buildDownloadUrl == null) {
                return@map BuildToProcess(
                    productCode = productCode,
                    buildFullNumber = it.buildFullNumber,
                    missingUrlReason = MissingUrlReason.NoLinuxDistribution
                )
            }

            return@map BuildToProcess(
                productCode = productCode,
                buildFullNumber = it.buildFullNumber,
                downloadUrl = downloadInfo.buildDownloadUrl
            )
        }
    }
}

private fun List<BuildInfo>.toMissingUrlBuilds(missingUrlReason: MissingUrlReason): List<BuildToProcess> =
    this.map {
        BuildToProcess(
            productCode = it.productCode,
            buildFullNumber = it.buildFullNumber,
            missingUrlReason = missingUrlReason
        )
    }