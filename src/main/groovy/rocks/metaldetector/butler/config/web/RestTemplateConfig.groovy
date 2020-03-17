package rocks.metaldetector.butler.config.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.impl.client.CloseableHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

import static java.nio.charset.StandardCharsets.UTF_8
import static org.springframework.http.MediaType.TEXT_HTML
import static org.springframework.http.MediaType.TEXT_PLAIN

@Configuration
class RestTemplateConfig {

  final CloseableHttpClient httpClient
  final String userAgent

  @Autowired
  RestTemplateConfig(CloseableHttpClient httpClient, @Value('${httpclient.useragent}') String userAgent) {
    this.httpClient = httpClient
    this.userAgent = userAgent
  }

  @Bean
  MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
    new MappingJackson2HttpMessageConverter(
            objectMapper: objectMapper()
    )
  }

  @Bean
  ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper(
            serializationInclusion: JsonInclude.Include.NON_EMPTY
    )

    return objectMapper
  }

  @Bean
  StringHttpMessageConverter stringHttpMessageConverter() {
    new StringHttpMessageConverter(
            defaultCharset: UTF_8,
            writeAcceptCharset: false,
            supportedMediaTypes: [TEXT_PLAIN, TEXT_HTML]
    )
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
            .interceptors(new CustomClientHttpRequestInterceptor(userAgent))
            .messageConverters([jackson2HttpMessageConverter(), stringHttpMessageConverter()])
            .build()
  }
}
