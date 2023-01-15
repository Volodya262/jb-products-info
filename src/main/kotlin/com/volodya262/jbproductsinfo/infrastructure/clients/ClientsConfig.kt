package com.volodya262.jbproductsinfo.infrastructure.clients

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class ClientsConfig {

    @Bean
    fun xmlProductInfoRestTemplate(): RestTemplate {
        // TODO connections pool
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory().apply {
            setConnectTimeout(5000)
            setConnectionRequestTimeout(5000)
            setReadTimeout(5000)
        }

        return RestTemplate(clientHttpRequestFactory)
    }

    @Bean
    fun distributionsRestTemplate(): RestTemplate {
        // TODO connections pool
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory().apply {
            setConnectTimeout(5000)
            setConnectionRequestTimeout(5000)
            setReadTimeout(5000)
        }

        return RestTemplate(clientHttpRequestFactory)
    }
}