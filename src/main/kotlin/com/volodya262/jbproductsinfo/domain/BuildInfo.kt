package com.volodya262.jbproductsinfo.domain

import java.time.LocalDate

class BuildInfo(
    val productCode: String,
    val channelId: String,
    val buildNumber: String,
    val releaseDate: LocalDate
)