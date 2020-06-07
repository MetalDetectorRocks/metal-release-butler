package rocks.metaldetector.butler.config.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    objectMapper.registerModule(new JavaTimeModule())
    objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

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
