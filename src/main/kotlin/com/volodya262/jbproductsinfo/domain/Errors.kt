package com.volodya262.jbproductsinfo.domain

abstract class BusinessLogicError(
    val errorCode: String,
    val readableMessage: String
) : RuntimeException("Business logic code: $errorCode; $readableMessage")

class ProductNotFound(
    productCode: ProductCode
) : BusinessLogicError(
    errorCode = "ProductNotFound",
    readableMessage = "Product not found by product code $productCode. Maybe it has only old builds"
)

class BuildNotFound(
    productCode: ProductCode,
    buildFullNumber: String
) : BusinessLogicError(
    errorCode = "BuildNotFound",
    readableMessage = "Build not found by productCode $productCode and buildFullNumber $buildFullNumber"
)

class WrongBuildProcessingStatus(
    productCode: ProductCode,
    buildFullNumber: String,
    status: BuildInProcessStatus
) : BusinessLogicError(
    errorCode = "WrongBuildProcessingStatus",
    readableMessage = "Build for productCode $productCode, buildFullNumber $buildFullNumber has status $status"
)

abstract class BuildProcessingError(
    val failedToProcessReason: FailedToProcessReason,
    errorCode: String,
    readableMessage: String
) : BusinessLogicError(errorCode, readableMessage)

class DistributionDownloadError(
    buildInProcess: BuildInProcess,
    statusCode: String
) : BuildProcessingError(
    errorCode = "DistributionDownloadError",
    readableMessage = "Error occured while downloading the distribution." +
        "Server responded $statusCode. build: {$buildInProcess}.",
    failedToProcessReason = FailedToProcessReason.DistributionDownloadError
)

class DistributionNotFound(
    productCode: ProductCode,
    buildFullNumber: String,
    url: String
) : BuildProcessingError(
    errorCode = "DistributionNotFound",
    readableMessage = "Distribution for product $productCode buildFullNumber: $buildFullNumber not found by url: $url.",
    failedToProcessReason = FailedToProcessReason.DistributionNotFoundByUrl
)

class TargetFileNotFound(
    val targetFileName: String,
    val build: BuildInProcess
) : BuildProcessingError(
    errorCode = "TargetFileNotFound",
    readableMessage = "File $targetFileName not found in distribution file of build $build",
    failedToProcessReason = FailedToProcessReason.TargetFileNotFound
)

class BuildResultsAreNotActual(
    val remoteBuild: BuildInProcess
) : BuildProcessingError(
    errorCode = "BuildResultsAreNotActual",
    readableMessage = "Expected Processing status, but remote build has status ${remoteBuild.status}",
    failedToProcessReason = FailedToProcessReason.BuildProcessResultsAreNotActual
)
