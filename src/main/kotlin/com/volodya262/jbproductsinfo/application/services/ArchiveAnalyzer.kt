package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.domain.TargetFileNotFound
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.springframework.stereotype.Component
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

@Component
class ArchiveAnalyzer {
    fun findFileContentsInTarGzArchive(tarGzFile: File, targetFileName: String): String {
        return usingTarArchiveInputStream(tarGzFile) { tarArchiveInputStream ->
            var entry: ArchiveEntry? = tarArchiveInputStream.nextEntry
            while (entry != null) {
                if (!tarArchiveInputStream.canReadEntryData(entry)) {
                    println("Failed to read entry data")
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

            throw TargetFileNotFound(targetFileName, "some description")
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