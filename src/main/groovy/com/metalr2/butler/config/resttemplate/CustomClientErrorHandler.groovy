package com.metalr2.butler.config.resttemplate

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class CustomClientErrorHandler implements ResponseErrorHandler {

  final Logger log = LoggerFactory.getLogger(CustomClientErrorHandler)

  @Override
  boolean hasError(ClientHttpResponse response) throws IOException {
    return response.statusCode.is4xxClientError()
  }

  @Override
  void handleError(ClientHttpResponse response) throws IOException {
    log.error("CustomClientErrorHandler | HTTP Status Code: " + response.statusCode.value())
  }

}
