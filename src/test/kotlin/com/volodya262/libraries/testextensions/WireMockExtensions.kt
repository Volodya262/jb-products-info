package com.volodya262.libraries.testextensions

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.matching.UrlPattern

fun stubForGet(urlPattern: UrlPattern, status: Int = 200, definition: ResponseDefinitionBuilder.() -> String) {
    val response = aResponse()
    val body = definition(response)
    stubFor(
        get(urlPattern)
            .willReturn(
                response
                    .withStatus(status)
                    .withBody(body)
            )
    )
}

fun stubForJsonGet(url: String, status: Int = 200, body: () -> String) {
    stubFor(
        get(urlEqualTo(url))
            .willReturn(
                aResponse()
                    .withStatus(status)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body())
            )
    )
}

fun stubForXmlGet(url: String, status: Int = 200, body: () -> String) {
    stubFor(
        get(urlEqualTo(url))
            .willReturn(
                aResponse()
                    .withStatus(status)
                    .withHeader("Content-Type", "text/xml")
                    .withBody(body())
            )
    )
}

