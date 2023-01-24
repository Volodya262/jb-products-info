package com.volodya262.jbproductsinfo.application.controllers

import com.volodya262.jbproductsinfo.application.repository.JdbcBuildsRepository
import com.volodya262.jbproductsinfo.application.repository.JdbcProductsRepository
import com.volodya262.jbproductsinfo.application.services.BuildQueueService
import com.volodya262.jbproductsinfo.domain.BuildInProcess
import com.volodya262.jbproductsinfo.domain.BuildInProcessEvent
import com.volodya262.jbproductsinfo.domain.BuildInProcessStatus
import com.volodya262.jbproductsinfo.domain.FailedToProcessReason
import com.volodya262.jbproductsinfo.domain.MissingUrlReason
import com.volodya262.jbproductsinfo.domain.ProductCode
import com.volodya262.jbproductsinfo.domain.ProductNotFound
import com.volodya262.jbproductsinfo.domain.WrongBuildProcessingStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@Profile("default", "default_api", "test")
class MainController(
    val buildQueueService: BuildQueueService,
    val jdbcProductsRepository: JdbcProductsRepository,
    val jdbcBuildsRepository: JdbcBuildsRepository
) {

    @GetMapping("/")
    fun statusAsView(model: Model): String {
        val status = getAppState()
        model.addAttribute("appState", status)
        return "/main"
    }

    @GetMapping("/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun status(): AppStateDto =
        getAppState()

    private fun getAppState(): AppStateDto {
        val products = jdbcProductsRepository.getProducts()
        val builds = jdbcBuildsRepository.getBuilds()
            .map { BuildInProcessDto.from(it) }
            .sortedByDescending { it.updatedAt }
            .groupBy { it.productCode }

        val productsDto = products.map {
            ProductDto(
                productCode = it.productCode,
                productName = it.productName,
                alternativeCodes = it.alternativeCodes,
                lastUpdate = it.lastUpdate,
                builds = builds[it.productCode] ?: emptyList()
            )
        }.sortedBy { it.productName }

        return AppStateDto(productsDto)
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

    @GetMapping("/{productCode}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getBuildsByProductCode(@PathVariable productCode: ProductCode): List<ProductBuildInfoDto> {

        val product = jdbcProductsRepository.getProducts()
            .find { it.productCode == productCode || it.alternativeCodes.contains(productCode) }
            ?: throw ProductNotFound(productCode)

        return jdbcBuildsRepository.getBuilds()
            .filter { it.productCode == product.productCode }
            .sortedByDescending { it.updatedAt }
            .map { ProductBuildInfoDto.from(it) }
    }

    @GetMapping("/{productCode}/{buildNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getBuildProductInfo(@PathVariable productCode: ProductCode, @PathVariable buildNumber: String): String {
        val product = jdbcProductsRepository.getProducts()
            .find { it.productCode == productCode || it.alternativeCodes.contains(productCode) }
            ?: throw ProductNotFound(productCode)

        val build = jdbcBuildsRepository.getBuild(product.productCode, buildNumber)
        if (build.status != BuildInProcessStatus.Processed) {
            throw WrongBuildProcessingStatus(productCode, buildNumber, build.status)
        }

        return build.targetFileContents!!
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

class AppStateDto(
    val products: List<ProductDto>
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
