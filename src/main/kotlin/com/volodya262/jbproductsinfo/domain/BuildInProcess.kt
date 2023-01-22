package com.volodya262.jbproductsinfo.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.Created
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.DownloadUrlUpdated
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.Empty
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.Expired
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.FailedToConstruct
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.FailedToProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.Processed
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.Processing
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus.Queued
import java.time.LocalDate
import java.time.OffsetDateTime

class BuildInProcess(
    val productCode: ProductCode,
    val buildFullNumber: String,
    private val currentDateTimeProvider: CurrentDateTimeProvider
) {
    private val events: MutableList<BuildInProcessEvent> = mutableListOf()
    val existingEvents: List<BuildInProcessEvent>
        get() = events

    val eventsToStore: MutableList<BuildInProcessEvent> = mutableListOf()
    private val nextEventNumber: Int
        get() = events.size + eventsToStore.size

    lateinit var updatedAt: OffsetDateTime

    var downloadUrl: String? = null
        private set

    var missingUrlReason: MissingUrlReason? = null
        private set

    var targetFileContents: String? = null
        private set

    var failedToProcessReason: FailedToProcessReason? = null
        private set

    var releaseDate: LocalDate? = null
        private set

    var status: BuildInProcessStatus = Empty
        private set

    companion object {
        fun fromEvents(
            productCode: ProductCode,
            buildFullNumber: String,
            events: List<BuildInProcessEvent>,
            currentDateTimeProvider: CurrentDateTimeProvider
        ): BuildInProcess {
            val sortedEvents = events.sortedBy { it.eventNumber }
            return BuildInProcess(productCode, buildFullNumber, currentDateTimeProvider)
                .apply { applyExisting(sortedEvents) }
        }
    }

    fun shouldRequeue(expireParams: BuildInProcessExpireParams): Boolean {
        return when (status) {
            Empty, Created -> true
            FailedToConstruct -> false
            DownloadUrlUpdated, Queued -> expireParams.queuedExpireMinutes.isExpired(updatedAt)
            Processing -> expireParams.processingExpireMinutes.isExpired(updatedAt)
            Processed -> false
            FailedToProcess -> failedToProcessReason!!.shouldRetry
                    && expireParams.failedToProcessExpireMinutes.isExpired(updatedAt)

            Expired -> false
        }
    }

    fun toCreated(downloadUrl: String, releaseDate: LocalDate) =
        applyNew(
            BuildInProcessCreatedEvent(
                nextEventNumber,
                currentDateTimeProvider.getOffsetDateTime(),
                downloadUrl,
                releaseDate
            )
        )

    fun toFailedToConstruct(missingUrlReason: MissingUrlReason) =
        applyNew(
            BuildInProcessFailedToConstructEvent(
                nextEventNumber,
                currentDateTimeProvider.getOffsetDateTime(),
                missingUrlReason
            )
        )

    fun toQueued() =
        applyNew(BuildInProcessQueuedEvent(nextEventNumber, currentDateTimeProvider.getOffsetDateTime()))

    fun toExpired() =
        applyNew(BuildInProcessExpiredEvent(nextEventNumber, currentDateTimeProvider.getOffsetDateTime()))

    fun toDownloadUrlUpdated(downloadUrl: String) =
        applyNew(
            BuildInProcessDownloadUrlUpdated(
                nextEventNumber,
                currentDateTimeProvider.getOffsetDateTime(),
                downloadUrl
            )
        )

    fun toProcessing() =
        applyNew(BuildInProcessProcessingEvent(nextEventNumber, currentDateTimeProvider.getOffsetDateTime()))

    fun toProcessed(targetFileContents: String) =
        applyNew(
            BuildInProcessProcessedEvent(
                nextEventNumber,
                currentDateTimeProvider.getOffsetDateTime(),
                targetFileContents
            )
        )

    fun toFailedToProcess(failedToProcessReason: FailedToProcessReason) =
        applyNew(
            BuildInProcessFailedToProcessEvent(
                nextEventNumber,
                currentDateTimeProvider.getOffsetDateTime(),
                failedToProcessReason
            )
        )

    private fun applyNew(event: BuildInProcessEvent) {
        eventsToStore.add(event)
        apply(event)
    }

    private fun applyExisting(events: List<BuildInProcessEvent>) {
        this.events.addAll(events)
        events.forEach { apply(it) }
    }

    private fun apply(event: BuildInProcessEvent) {
        updatedAt = event.createdAt

        when (event) {
            is BuildInProcessCreatedEvent -> {
                status = Created
                downloadUrl = event.downloadUrl
                releaseDate = event.releaseDate
            }

            is BuildInProcessFailedToConstructEvent -> {
                status = FailedToConstruct
                missingUrlReason = event.missingUrlReason
            }

            is BuildInProcessQueuedEvent -> status = Queued
            is BuildInProcessProcessingEvent -> status = Processing
            is BuildInProcessProcessedEvent -> {
                status = Processed
                targetFileContents = event.targetFileContents
            }

            is BuildInProcessFailedToProcessEvent -> {
                status = FailedToProcess
                failedToProcessReason = event.failedToProcessReason
            }

            is BuildInProcessExpiredEvent -> status = Expired
            is BuildInProcessDownloadUrlUpdated -> {
                status = DownloadUrlUpdated
                downloadUrl = event.downloadUrl
                missingUrlReason = null
            }
        }
    }

    fun applyEventsSaved() {
        events.addAll(eventsToStore)
        eventsToStore.clear()
    }

    override fun toString(): String {
        return "BuildInProcess(productCode='$productCode', buildFullNumber='$buildFullNumber', " +
                "updatedAt=$updatedAt, downloadUrl=$downloadUrl, missingUrlReason=$missingUrlReason, " +
                "targetFileContents=${targetFileContents?.take(50)}, failedToProcessReason=$failedToProcessReason, " +
                "releaseDate=$releaseDate, status=$status)"
    }
}

enum class BuildInProcessStatus {
    Empty,
    Created,
    FailedToConstruct,
    Queued,
    Processing,
    Processed,
    FailedToProcess,
    Expired,
    DownloadUrlUpdated
}

enum class FailedToProcessReason {
    TargetFileNotFound,
    DistributionNotFoundByUrl,
    DistributionDownloadError,
    BuildProcessResultsAreNotActual,
    IOException,
    InternalError;

    val shouldRetry: Boolean
        get() = when (this) {
            DistributionDownloadError, IOException -> true
            else -> false
        }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "class")
sealed class BuildInProcessEvent(val eventNumber: Int, val createdAt: OffsetDateTime)

class BuildInProcessCreatedEvent(
    eventNumber: Int,
    createdAt: OffsetDateTime,
    val downloadUrl: String,
    val releaseDate: LocalDate
) : BuildInProcessEvent(eventNumber, createdAt)

class BuildInProcessFailedToConstructEvent(
    eventNumber: Int,
    createdAt: OffsetDateTime,
    val missingUrlReason: MissingUrlReason
) : BuildInProcessEvent(eventNumber, createdAt)

class BuildInProcessQueuedEvent(
    eventNumber: Int,
    createdAt: OffsetDateTime
) : BuildInProcessEvent(eventNumber, createdAt)

class BuildInProcessProcessingEvent(
    eventNumber: Int,
    createdAt: OffsetDateTime
) : BuildInProcessEvent(eventNumber, createdAt)

class BuildInProcessProcessedEvent(
    eventNumber: Int,
    createdAt: OffsetDateTime,
    val targetFileContents: String
) : BuildInProcessEvent(eventNumber, createdAt)

class BuildInProcessFailedToProcessEvent(
    eventNumber: Int,
    createdAt: OffsetDateTime,
    val failedToProcessReason: FailedToProcessReason
) : BuildInProcessEvent(eventNumber, createdAt)

class BuildInProcessExpiredEvent(eventNumber: Int, createdAt: OffsetDateTime) :
    BuildInProcessEvent(eventNumber, createdAt)

class BuildInProcessDownloadUrlUpdated(eventNumber: Int, createdAt: OffsetDateTime, val downloadUrl: String) :
    BuildInProcessEvent(eventNumber, createdAt)
