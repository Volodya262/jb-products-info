package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.TargetFileNotFound
import com.volodya262.jbproductsinfo.infrastructure.RealCurrentDateTimeProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Paths
import java.time.LocalDate

class ArchiveAnalyzerTest {

    private val jsonContents = "{ \"field1\": \"value1\", \"field2\": 123 }"
    private val targetFileName = "product-info.json"

    @Test
    fun `should find target file in tar gz archive and return its contents`() {
        val archiveAnalyzer = ArchiveAnalyzer()
        val file = getResourceAsFile("archive-with-product-info.tar.gz")
        val foundFileContents = archiveAnalyzer.findFileContentsInTarGzArchive(file, targetFileName, createDummyBuildInProcess())
        assertEquals(jsonContents.trim(), foundFileContents.trim())
    }

    @Test
    fun `should throw TargetFileNotFound if file not found`() {
        val archiveAnalyzer = ArchiveAnalyzer()
        val file = getResourceAsFile("archive-with-product-info.tar.gz")
        assertThrows<TargetFileNotFound> {
            archiveAnalyzer.findFileContentsInTarGzArchive(file, "bla.txt", createDummyBuildInProcess())
        }
    }

    private fun getResourceAsFile(name: String): File {
        val resource = this.javaClass.classLoader!!.getResource(name)!!
        return Paths.get(resource.path).toFile()
    }

    private fun createDummyBuildInProcess() =
        BuildInProcess("IC", "111.222.333", RealCurrentDateTimeProvider())
            .apply {
                toCreated("ya.ru", LocalDate.now())
                toProcessing()
            }

}
