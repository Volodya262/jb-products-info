package com.volodya262.jbproductsinfo.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class ClientsConfig(
    val objectMapper: ObjectMapper,
    @Value("\${client.max-total-connections}")
    val maxTotalConnections: Int,
    @Value("\${client.default-max-per-route}")
    val defaultMaxPerRoute: Int
) {

    @Bean
    fun jetBrainsUpdatesRestTemplate(): RestTemplate {
        val connectionManager = PoolingHttpClientConnectionManager().also {
            it.maxTotal = maxTotalConnections
            it.defaultMaxPerRoute = defaultMaxPerRoute
        }

        val httpClient: HttpClient = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .build()

        return RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(5000))
            .setReadTimeout(Duration.ofMillis(5000))
            .requestFactory { HttpComponentsClientHttpRequestFactory(httpClient) }
            .build()
    }

    @Bean
    fun jetBrainsDataServicesRestTemplate(): RestTemplate {
        val connectionManager = PoolingHttpClientConnectionManager().also {
            it.maxTotal = maxTotalConnections
            it.defaultMaxPerRoute = defaultMaxPerRoute
        }

        val httpClient: HttpClient = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .build()

        return RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(5000))
            .setReadTimeout(Duration.ofMillis(5000))
            .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .requestFactory { HttpComponentsClientHttpRequestFactory(httpClient) }
            .build()
    }

    @Bean
    fun distributionsRestTemplate(): RestTemplate {
        val connectionManager = PoolingHttpClientConnectionManager().also {
            it.maxTotal = 10
            it.defaultMaxPerRoute = 10
        }

        val httpClient: HttpClient = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .build()

        return RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(5000))
            .setReadTimeout(Duration.ofMillis(5000))
            .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .requestFactory { HttpComponentsClientHttpRequestFactory(httpClient) }
            .setBufferRequestBody(false)
            .build()
    }
}
