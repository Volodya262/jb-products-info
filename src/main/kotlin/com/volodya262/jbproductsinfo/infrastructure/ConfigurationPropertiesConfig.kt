package com.volodya262.jbproductsinfo.infrastructure

import com.volodya262.jbproductsinfo.domain.BuildInProcessExpireParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConfigurationPropertiesConfig {

    @Bean
    fun buildInProcessExpireParams(
        @Value("\${builds-in-process-expire.queued-expire-minutes}")
        queuedExpireMinutes: Int,
        @Value("\${builds-in-process-expire.queued-expire-minutes}")
        processingExpireMinutes: Int,
        @Value("\${builds-in-process-expire.queued-expire-minutes}")
        failedToProcessExpireMinutes: Int
    ) = BuildInProcessExpireParams(
        queuedExpireMinutes = queuedExpireMinutes,
        processingExpireMinutes = processingExpireMinutes,
        failedToProcessExpireMinutes = failedToProcessExpireMinutes
    )
}
