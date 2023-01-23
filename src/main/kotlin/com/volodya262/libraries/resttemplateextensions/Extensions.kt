package com.volodya262.libraries.resttemplateextensions

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import java.net.URI

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

inline fun <reified T> RestTemplate.getForObjectReified(url: String): T? {
    val requestEntity = RequestEntity<Any>(HttpMethod.GET, URI(url))
    return this.exchange(requestEntity, typeReference<T>()).body
}
