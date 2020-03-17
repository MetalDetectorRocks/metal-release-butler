package rocks.metaldetector.butler.config.resttemplate

import groovy.util.logging.Slf4j
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

@Slf4j
class CustomClientErrorHandler implements ResponseErrorHandler {

  @Override
  boolean hasError(ClientHttpResponse response) {
    return response.statusCode.'4xxClientError' || response.statusCode.'5xxServerError'
  }

  @Override
  void handleError(ClientHttpResponse response) throws IOException {
  }

  @Override
  void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
    def logMessage = "URL: ${url.toString()} | " +
            "Method: ${method.name()} | " +
            "Status code: ${response.statusCode.value()} | " +
            "Status text: ${response.statusText}"

    response.statusCode.'5xxServerError' ? log.error(logMessage) : log.warn(logMessage)
  }
}
