package com.volodya262.libraries.testutils

import java.io.File
import java.io.InputStream
import java.nio.file.Paths

fun getResourceAsFile(javaClass: Class<Any>, name: String): File {
    val resource = javaClass.classLoader!!.getResource(name)!!
    return Paths.get(resource.path).toFile()
}

fun getResourceAsStream(javaClass: Class<Any>, name: String): InputStream {
    return javaClass.classLoader!!.getResourceAsStream(name)!!
}
