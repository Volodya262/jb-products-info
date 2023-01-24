package com.volodya262.jbproductsinfo.domain

import java.time.LocalDate
import java.time.OffsetDateTime

typealias ProductCode = String

data class Product(
    val productCode: ProductCode,
    val productName: String,
    val alternativeCodes: Set<String>
)

data class LocalProduct( // represents product that exists in this services DB
    val productCode: ProductCode,
    val productName: String,
    val alternativeCodes: Set<String>,
    val lastUpdate: OffsetDateTime
)

data class ProductAndBuildDownloadInfos(
    val product: Product,
    val productReleases: List<ProductRelease>
)

class ProductRelease(
    val productCode: ProductCode,
    val buildReleaseDate: LocalDate,
    val buildVersion: String,
    val buildFullNumber: String,
    val downloadUrl: String?
)

data class FamilyGroupBuilds(
    // one build may correspond to different products, like Intellij IDEA Ultimate and Community
    val relatedProductCodes: Set<ProductCode>,
    val familyGroupName: String,
    val builds: List<BuildInfo>
)

data class BuildInfo(
    val productCode: String,
    val buildReleaseDate: LocalDate,
    val buildVersion: String,
    val buildFullNumber: String
)

enum class MissingUrlReason {
    NoLinuxDistribution,
    FailedToFindAssociatedVersion
}
