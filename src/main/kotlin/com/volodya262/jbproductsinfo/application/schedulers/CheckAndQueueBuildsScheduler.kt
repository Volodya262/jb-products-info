package com.volodya262.jbproductsinfo.application.schedulers

import com.volodya262.jbproductsinfo.application.services.BuildQueueService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class CheckAndQueueBuildsScheduler(
    private val buildQueueService: BuildQueueService
) {

    val scheduler = createScheduler()

    val logger: Logger = LoggerFactory.getLogger(CheckAndQueueBuildsScheduler::class.java)

    @PostConstruct
    fun schedule() {
        val cronTrigger = CronTrigger("0 0 * * * *")
        scheduler.schedule(
            {
                logger.info("Refreshing build info on schedule...")
                val res = buildQueueService.checkAndQueueBuilds()
                logger.info("Refreshed build info. Queued ${res.size} builds. $res")
            },
            cronTrigger
        )
    }

    private fun createScheduler(): ThreadPoolTaskScheduler =
        ThreadPoolTaskScheduler()
            .apply {
                poolSize = 1
                setThreadNamePrefix("CheckAndQueueBuildsScheduler")
            }.apply {
                initialize()
            }
}
