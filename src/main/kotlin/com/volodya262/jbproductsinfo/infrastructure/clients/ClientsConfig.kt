package com.volodya262.jbproductsinfo.infrastructure.clients

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class ClientsConfig(
    val objectMapper: ObjectMapper
) {

    @Bean
    fun jetBrainsUpdatesRestTemplate(): RestTemplate {
        // TODO connections pool
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory().apply {
            setConnectTimeout(5000)
            setConnectionRequestTimeout(5000)
            setReadTimeout(5000)
        }

        return RestTemplate(clientHttpRequestFactory)
    }

    @Bean
    fun jetBrainsDataServicesRestTemplate(): RestTemplate {
        // TODO connections pool
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory().apply {
            setConnectTimeout(5000)
            setConnectionRequestTimeout(5000)
            setReadTimeout(5000)
        }

        return RestTemplateBuilder()
            .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .requestFactory { clientHttpRequestFactory }
            .build()
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