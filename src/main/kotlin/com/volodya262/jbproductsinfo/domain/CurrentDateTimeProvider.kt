package com.volodya262.jbproductsinfo.domain

import java.time.LocalDate
import java.time.OffsetDateTime

interface CurrentDateTimeProvider {
    fun getLocalDate(): LocalDate
    fun getOffsetDateTime(): OffsetDateTime
}