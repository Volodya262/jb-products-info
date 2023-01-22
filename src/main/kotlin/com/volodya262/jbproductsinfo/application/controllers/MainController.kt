package com.volodya262.jbproductsinfo.application.controllers

import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.repository.JdbcProductsRepository
import com.volodya262.jbproductsinfo.application.services.BuildQueueService
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessEvent
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus
import com.volodya262.jbproductsinfo.domain.BuildNotFound
import com.volodya262.jbproductsinfo.domain.FailedToProcessReason
import com.volodya262.jbproductsinfo.domain.MissingUrlReason
import com.volodya262.jbproductsinfo.domain.ProductCode
import com.volodya262.jbproductsinfo.domain.WrongBuildProcessingStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate
import java.time.OffsetDateTime

@Controller
class MainController(
    val buildQueueService: BuildQueueService,
    val jdbcProductsRepository: JdbcProductsRepository,
    val jdbcBuildsRepository: JdbcBuildsRepository
) {

    @GetMapping("/")
    fun statusAsView(model: Model): String {
        val status = getStatusDto()
        model.addAttribute("status", status)
        return "/main"
    }

    @GetMapping("/status")
    @ResponseBody
    fun status(): StatusDto =
        getStatusDto()

    private fun getStatusDto(): StatusDto {
        val products = jdbcProductsRepository.getProducts()
        val builds = jdbcBuildsRepository.getBuilds()
            .map { BuildInProcessDto.from(it) }
            .sortedByDescending { it.updatedAt }
            .groupBy { it.productCode }

        val productsDto =  products.map {
            ProductDto(
                productCode = it.productCode,
                productName = it.productName,
                alternativeCodes = it.alternativeCodes,
                lastUpdate = it.lastUpdate,
                builds = builds[it.productCode] ?: emptyList()
            )
        }

        return StatusDto(productsDto)
    }

    @PostMapping("/refresh")
    @ResponseBody
    fun refresh() {
        buildQueueService.checkAndQueueBuilds()
    }

    @PostMapping("/refresh/{productCode}")
    @ResponseBody
    fun refresh(@PathVariable productCode: ProductCode) {
        buildQueueService.checkAndQueueBuilds(productCode)
    }

    @GetMapping("/{productCode}")
    @ResponseBody
    fun getBuildsByProductCode(@PathVariable productCode: ProductCode): List<ProductBuildInfoDto> {

        return jdbcBuildsRepository.getBuilds()
            .sortedByDescending { it.updatedAt }
            .map { ProductBuildInfoDto.from(it) }
    }

    @GetMapping("/{productCode}/{buildNumber}")
    @ResponseBody
    fun getBuildProductInfo(@PathVariable productCode: ProductCode, @PathVariable buildNumber: String): ResponseEntity<Any> {
        return try {
            val build = jdbcBuildsRepository.getBuild(productCode, buildNumber)
            if (build.status != BuildInProcessStatus.Processed) {
                throw WrongBuildProcessingStatus(productCode, buildNumber, build.status)
            }

            ResponseEntity.ok(build.targetFileContents!!)
        } catch (ex: BuildNotFound) {
            ResponseEntity(ex, HttpStatus.NOT_FOUND)
        }
    }
}

class ProductBuildInfoDto(
    val buildNumber: String,
    val productInfo: String?
) {
    companion object {
        fun from(buildInProcess: BuildInProcess): ProductBuildInfoDto =
            ProductBuildInfoDto(
                buildNumber = buildInProcess.buildFullNumber,
                productInfo = buildInProcess.targetFileContents
            )
    }
}

class StatusDto(
    val products: List<ProductDto>,

)

class ProductDto(
    val productCode: ProductCode,
    val productName: String,
    val alternativeCodes: Set<String>,
    val lastUpdate: OffsetDateTime,
    val builds: List<BuildInProcessDto>
)

class BuildInProcessDto(
    val productCode: ProductCode,
    val buildNumber: String,
    var status: BuildInProcessStatus,
    val updatedAt: OffsetDateTime,
    val missingUrlReason: MissingUrlReason?,
    val targetFileContents: String?,
    var failedToProcessReason: FailedToProcessReason?,
    var releaseDate: LocalDate?,
    val events: List<BuildInProcessEvent>
) {
    companion object {
        fun from(buildInProcess: BuildInProcess) =
            BuildInProcessDto(
                productCode = buildInProcess.productCode,
                buildNumber = buildInProcess.buildFullNumber,
                status = buildInProcess.status,
                updatedAt = buildInProcess.updatedAt,
                missingUrlReason = buildInProcess.missingUrlReason,
                targetFileContents = buildInProcess.targetFileContents,
                failedToProcessReason = buildInProcess.failedToProcessReason,
                releaseDate = buildInProcess.releaseDate,
                events = buildInProcess.existingEvents + buildInProcess.eventsToStore
            )
    }

    override fun toString(): String {
        return "BuildInProcessDto(productCode='$productCode', buildNumber='$buildNumber', status=$status, " +
            "updatedAt=$updatedAt, missingUrlReason=$missingUrlReason, targetFileContents=$targetFileContents, " +
            "failedToProcessReason=$failedToProcessReason, releaseDate=$releaseDate, events=$events)"
    }


}
