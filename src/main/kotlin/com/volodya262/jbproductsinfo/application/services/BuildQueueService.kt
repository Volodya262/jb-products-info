package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.application.publishers.KafkaBuildsToProcessPublisher
import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.repository.JdbcProductsRepository
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessExpireParams
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus
import com.volodya262.jbproductsinfo.domain.CurrentDateTimeProvider
import com.volodya262.jbproductsinfo.domain.ProductCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("default", "default_api", "test")
class BuildQueueService(
    private val remoteBuildsInfoProviderService: RemoteBuildsInfoProviderService,
    private val jdbcProductsRepository: JdbcProductsRepository,
    private val jdbcBuildsRepository: JdbcBuildsRepository,
    private val kafkaBuildsToProcessPublisher: KafkaBuildsToProcessPublisher,
    private val buildInProcessExpireParams: BuildInProcessExpireParams,
    @Value("\${target-builds-age-days}")
    private val targetBuildsAgeDays: Long,
    private val currentDateTimeProvider: CurrentDateTimeProvider
) {

    val logger: Logger = LoggerFactory.getLogger(BuildQueueService::class.java)

    fun checkAndQueueBuilds(productCode: ProductCode? = null): List<BuildInProcess> {
        val buildExpireDate = currentDateTimeProvider.getLocalDate().minusDays(targetBuildsAgeDays)
        val (products, buildsToProcessMap) = remoteBuildsInfoProviderService.getProductsBuilds(buildExpireDate, productCode)

        jdbcProductsRepository.updateLocalProducts(products)

        val remoteBuilds = buildsToProcessMap.values
            .flatten()
            .filter { it.releaseDate != null && it.releaseDate!!.isAfter(buildExpireDate) }
            .distinctBy { it.generateId() }

        val existingBuildsMap = jdbcBuildsRepository.getBuilds().associateBy { it.generateId() }

        val buildsToQueue = remoteBuilds
            .map { remoteBuild ->
                val existingBuild = existingBuildsMap[remoteBuild.generateId()] ?: return@map remoteBuild

                if (existingBuild.status == BuildInProcessStatus.FailedToConstruct && remoteBuild.downloadUrl != null) {
                    return@map existingBuild.apply { toDownloadUrlUpdated(remoteBuild.downloadUrl!!) }
                }

                if (existingBuild.shouldRequeue(buildInProcessExpireParams)) {
                    logger.info("Mark build is expired {}", existingBuild)
                    return@map existingBuild.apply { toExpired() }
                } else {
                    logger.info("Skipping build {}", existingBuild)
                    return@map null
                }
            }.filterNotNull()

        if (buildsToQueue.isEmpty()) {
            return emptyList()
        }

        buildsToQueue.forEach { it.toQueued() }
        jdbcBuildsRepository.saveNew(buildsToQueue)
        buildsToQueue.forEach { it.applyEventsSaved() }
        kafkaBuildsToProcessPublisher.publish(buildsToQueue)

        return buildsToQueue
    }
}

private fun BuildInProcess.generateId() =
    "$productCode:$buildFullNumber"
