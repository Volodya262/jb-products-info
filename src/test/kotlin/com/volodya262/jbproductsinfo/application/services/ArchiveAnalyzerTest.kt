package com.volodya262.jbproductsinfo.application.services
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.TargetFileNotFound
import com.volodya262.jbproductsinfo.infrastructure.RealCurrentDateTimeProvider
import com.volodya262.libraries.testutils.getResourceAsFile
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArchiveAnalyzerTest {

    private val jsonContents = "{ \"field1\": \"value1\", \"field2\": 123 }"
    private val targetFileName = "product-info.json"

    @Test
    fun `should find target file in tar gz archive and return its contents`() {
        val archiveAnalyzer = ArchiveAnalyzer()
        val file = getResourceAsFile(this.javaClass, "archive-with-product-info.tar.gz")
        val foundFileContents = archiveAnalyzer.findFileContentsInTarGzArchive(file, targetFileName, createDummyBuildInProcess())
        assertEquals(jsonContents.trim(), foundFileContents.trim())
    }

    @Test
    fun `should throw TargetFileNotFound if file not found`() {
        val archiveAnalyzer = ArchiveAnalyzer()
        val file = getResourceAsFile(this.javaClass, "archive-with-product-info.tar.gz")
        assertThrows<TargetFileNotFound> {
            archiveAnalyzer.findFileContentsInTarGzArchive(file, "bla.txt", createDummyBuildInProcess())
        }
    }

    private fun createDummyBuildInProcess() =
        BuildInProcess("IC", "111.222.333", RealCurrentDateTimeProvider())
            .apply {
                toCreated("ya.ru", LocalDate.now())
                toProcessing()
            }
}
