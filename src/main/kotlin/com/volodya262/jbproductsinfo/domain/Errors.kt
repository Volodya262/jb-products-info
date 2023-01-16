package com.volodya262.jbproductsinfo.domain

open class BusinessLogicError(
    val errorCode: String,
    val readableMessage: String
) : RuntimeException("Business logic code: $errorCode; $readableMessage")

class DistributionNotFound(
    url: String,
    distributionName: String
) : BusinessLogicError(
    errorCode = "DistributionNotFound",
    readableMessage = "Distribution $distributionName not found by url: $url."
)