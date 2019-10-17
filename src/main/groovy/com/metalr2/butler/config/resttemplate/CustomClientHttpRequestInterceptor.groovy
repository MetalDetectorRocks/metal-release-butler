package com.metalr2.butler.config.resttemplate

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class CustomClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  // ToDo DanielW: Inject from properties
  static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36"

  final Logger LOG = LoggerFactory.getLogger(CustomClientErrorHandler)

  @Override
  ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().setAccept([MediaType.APPLICATION_JSON])
    request.getHeaders().set("User-Agent", USER_AGENT)

    LOG.info("URI: {}", request.getURI())
    LOG.info("Headers: {}", request.getHeaders())

    return execution.execute(request, body)
  }

}
