package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.utils.generateTempFileName
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus
import com.volodya262.jbproductsinfo.domain.BuildProcessingError
import com.volodya262.jbproductsinfo.domain.BuildResultsAreNotActual
import com.volodya262.jbproductsinfo.domain.FailedToProcessReason
import com.volodya262.jbproductsinfo.domain.ProductCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Component
class BuildProcessorService(
    val jdbcBuildsRepository: JdbcBuildsRepository,
    val distributionDownloader: DistributionDownloader,
    val archiveAnalyzer: ArchiveAnalyzer
) {

    companion object {
        private const val targetFileName = "product-info.json"
    }

    val logger: Logger = LoggerFactory.getLogger(BuildProcessorService::class.java)

    // returns true if listener should ack and false otherwise
    fun process(productCode: ProductCode, buildFullNumber: String): Boolean {
        logger.info("Preparing to process build (productCode: $productCode, buildFullNumber: $buildFullNumber)")
        val buildInProcess = jdbcBuildsRepository.getBuild(productCode, buildFullNumber)

        if (buildInProcess.status != BuildInProcessStatus.Queued) {
            logger.info("Skipping build. Expected Queued but got ${buildInProcess.status}. {}", buildInProcess)
            return true
        }

        buildInProcess.toProcessing()
        buildInProcess.save(jdbcBuildsRepository)

        try {
            logger.info("Started processing build {}", buildInProcess)

            val tempFile = File.createTempFile("download-", buildInProcess.generateTempFileName())
            FileOutputStream(tempFile).use { fileOutputStream ->
                distributionDownloader.downloadDistributionToOutputStream(buildInProcess, fileOutputStream)
            }
            val fileContents = archiveAnalyzer.findFileContentsInTarGzArchive(tempFile, targetFileName, buildInProcess)
            logger.info("Found file contents for build {}", buildInProcess)

            // around 10 minutes has passed since start, so we have to make sure that this task is still actual
            val actualBuildInProcess = jdbcBuildsRepository.getBuild(productCode, buildFullNumber)
            actualBuildInProcess.ensureStillActual()
            buildInProcess.toProcessed(fileContents)
            buildInProcess.save(jdbcBuildsRepository)

            tryDeleteTempFile(tempFile)

            logger.info("Build processed. {}", buildInProcess)
            return true
        } catch (ex: BuildProcessingError) {
            logger.error("Caught BuildProcessingError. build: {}", buildInProcess, ex)
            buildInProcess.toFailedToProcess(ex.failedToProcessReason)
            buildInProcess.save(jdbcBuildsRepository)
            return !ex.failedToProcessReason.shouldRetry
        } catch (ex: ResourceAccessException) {
            logger.error("Caught ResourceAccessException. build: {}", buildInProcess, ex)
            buildInProcess.toFailedToProcess(FailedToProcessReason.DistributionDownloadError)
            buildInProcess.save(jdbcBuildsRepository)
            return !FailedToProcessReason.DistributionDownloadError.shouldRetry
        } catch (ex: IOException) {
            logger.error("Caught IOException. build: {}", buildInProcess, ex)
            buildInProcess.toFailedToProcess(FailedToProcessReason.IOException)
            buildInProcess.save(jdbcBuildsRepository)
            return !FailedToProcessReason.IOException.shouldRetry
        } catch (ex: Exception) {
            logger.error("Caught unknown exception. build: {}", buildInProcess, ex)
            buildInProcess.toFailedToProcess(FailedToProcessReason.InternalError)
            buildInProcess.save(jdbcBuildsRepository)
            return !FailedToProcessReason.InternalError.shouldRetry
        }
    }

    private fun tryDeleteTempFile(tempFile: File) {
        try {
            tempFile.deleteOnExit()
        } catch (ex: Exception) {
            logger.error("Failed to delete temp file $tempFile", ex)
        }
    }
}

private fun BuildInProcess.ensureStillActual() {
    if (this.status != BuildInProcessStatus.Processing) {
        throw BuildResultsAreNotActual(this)
    }
}

private fun BuildInProcess.save(jdbcBuildsRepository: JdbcBuildsRepository) {
    jdbcBuildsRepository.save(this)
    this.applyEventsSaved()
}
