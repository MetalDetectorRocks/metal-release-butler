package com.metalr2.butler.config.resttemplate

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class CustomClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  final Logger LOG = LoggerFactory.getLogger(CustomClientErrorHandler.class)

  @Override
  ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON))
    request.getHeaders().set("User-Agent", "") // ToDo DanielW: set user agent

    LOG.info("URI: {}", request.getURI())
    LOG.info("Headers: {}", request.getHeaders())

    return execution.execute(request, body)
  }

}
