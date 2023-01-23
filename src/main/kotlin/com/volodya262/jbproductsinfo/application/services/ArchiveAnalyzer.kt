package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.TargetFileNotFound
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

@Component
@Profile("default", "default_worker", "test")
class ArchiveAnalyzer {

    val logger: Logger = LoggerFactory.getLogger(ArchiveAnalyzer::class.java)

    fun findFileContentsInTarGzArchive(
        tarGzFile: File,
        targetFileName: String,
        buildInProcess: BuildInProcess
    ): String {
        try {
            return usingTarArchiveInputStream(tarGzFile) { tarArchiveInputStream ->
                var entry: ArchiveEntry? = tarArchiveInputStream.nextEntry
                while (entry != null) {
                    if (!tarArchiveInputStream.canReadEntryData(entry)) {
                        logger.warn(
                            "Failed to read entry data. ProductCode: {}, buildFullNumber: {}",
                            buildInProcess.productCode,
                            buildInProcess.buildFullNumber
                        )
                        continue
                    }

                    val entryFileName = Paths.get(entry.name).fileName.toString()
                    if (entryFileName == targetFileName) {
                        return@usingTarArchiveInputStream String(
                            bytes = tarArchiveInputStream.readAllBytes(),
                            charset = StandardCharsets.UTF_8
                        )
                    }

                    entry = tarArchiveInputStream.nextEntry
                }

                throw TargetFileNotFound(targetFileName, buildInProcess)
            }
        } catch (ex: IOException) {
            logger.error("IOException occured while analyzing build: $buildInProcess", ex)
            throw ex
        }
    }
}

fun <T> usingTarArchiveInputStream(inputFile: File, block: (tarArchiveInputStream: TarArchiveInputStream) -> T): T {
    FileInputStream(inputFile).use { fileInputStream ->
        BufferedInputStream(fileInputStream).use { bufferedInputStream ->
            GzipCompressorInputStream(bufferedInputStream).use { gzipCompressorInputStream ->
                TarArchiveInputStream(gzipCompressorInputStream).use { tarArchiveInputStream ->
                    return block(tarArchiveInputStream)
                }
            }
        }
    }
}
