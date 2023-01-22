package com.volodya262.jbproductsinfo.domain

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class BuildInProcessExpireParams(
    val queuedExpireMinutes: Int,
    val processingExpireMinutes: Int,
    val failedToProcessExpireMinutes: Int
)

fun Int.isExpired(offsetDateTime: OffsetDateTime) =
    ChronoUnit.MINUTES.between(OffsetDateTime.now(), offsetDateTime) > this
