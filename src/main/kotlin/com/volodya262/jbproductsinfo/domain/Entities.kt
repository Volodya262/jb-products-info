package com.volodya262.jbproductsinfo.domain

import java.time.LocalDate


class BuildInfo(
    val productCode: String,
    val releaseDate: LocalDate,
    val buildVersion: String,
    val buildNumber: String
)

class BuildDownloadInfo(
    val productCode: String,
    val releaseDate: LocalDate,
    val version: String,
    val downloadUrl: String
)

data class ProductsInfo(
    val products: List<Product>, val builds: Map<ProductCode, List<BuildInfo>>
)

typealias ProductCode = String

class Product(
    val productCode: ProductCode, val productName: String
)

