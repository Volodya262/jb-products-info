package com.volodya262.jbproductsinfo.application.dto

import com.volodya262.jbproductsinfo.domain.BuildInProcess

data class BuildToProcessKafkaMessage(
    val productCode: String,
    val buildFullNumber: String
) {
    companion object {
        fun from(build: BuildInProcess): BuildToProcessKafkaMessage =
            BuildToProcessKafkaMessage(build.productCode, build.buildFullNumber)
    }

    fun generateKafkaKey() =
        "$productCode:$buildFullNumber"
}
