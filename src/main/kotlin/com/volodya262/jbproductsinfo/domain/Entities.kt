package com.volodya262.jbproductsinfo.domain

import java.time.LocalDate

typealias ProductCode = String

data class Product( // merged product
    val productCode: ProductCode,
    val productName: String,
    val alternativeCodes: Set<String>
)

data class ProductAndBuildDownloadInfos(
    val product: Product,
    val buildDownloadInfos: List<BuildDownloadInfo>
)

class BuildDownloadInfo(
    val productCode: ProductCode,
    val buildReleaseDate: LocalDate,
    val buildVersion: String,
    val buildFullNumber: String,
    val buildDownloadUrl: String?
)

//data class BuildGroupProduct( // from updates service
//    val relatedProductCodes: Set<ProductCode>, // one build may correspond to different products, like Intellij IDEA Ultimate and Community
//    val productName: String
//)

data class FamilyGroupBuilds(
    val relatedProductCodes: Set<ProductCode>, // one build may correspond to different products, like Intellij IDEA Ultimate and Community
    val familyGroupName: String,
    val builds: List<BuildInfo>
)

data class BuildInfo(
    val productCode: String,
    val buildReleaseDate: LocalDate,
    val buildVersion: String,
    val buildFullNumber: String
)

class BuildToProcess(
    val productCode: String,
    val buildFullNumber: String,
    val downloadUrl: String? = null,
    val missingUrlReason: MissingUrlReason? = null
)

enum class MissingUrlReason {
    NoLinuxDistribution,
    FailedToFindAssociatedVersion
}

