package com.volodya262.jbproductsinfo.application.utils

import com.volodya262.jbproductsinfo.domain.BuildInProcess
import java.net.URI
import java.nio.file.Paths
import java.util.UUID

fun BuildInProcess.generateTempFileName() =
    """${UUID.randomUUID()}-${Paths.get(URI(downloadUrl!!).path).fileName}"""
