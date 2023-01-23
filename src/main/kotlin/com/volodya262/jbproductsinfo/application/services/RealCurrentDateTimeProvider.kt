package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.domain.CurrentDateTimeProvider
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.OffsetDateTime

@Component
class RealCurrentDateTimeProvider : CurrentDateTimeProvider {
    override fun getLocalDate(): LocalDate = LocalDate.now()

    override fun getOffsetDateTime(): OffsetDateTime = OffsetDateTime.now()
}
