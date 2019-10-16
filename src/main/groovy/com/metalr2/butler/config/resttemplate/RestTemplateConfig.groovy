package com.metalr2.butler.config.resttemplate

import org.apache.http.impl.client.CloseableHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

  final CloseableHttpClient httpClient

  @Autowired
  RestTemplateConfig(CloseableHttpClient httpClient) {
    this.httpClient = httpClient
  }

  @Bean
  HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory()
    clientHttpRequestFactory.httpClient = httpClient
    return clientHttpRequestFactory
  }

  @Bean
  RestTemplate restTemplate() {
    return new RestTemplateBuilder()
            .requestFactory({ -> clientHttpRequestFactory() })
            .errorHandler(new CustomClientErrorHandler())
            .interceptors(new CustomClientHttpRequestInterceptor())
            .build()
  }
}
