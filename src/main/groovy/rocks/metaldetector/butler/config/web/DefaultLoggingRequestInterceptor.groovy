package rocks.metaldetector.butler.config.web

import groovy.util.logging.Slf4j
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

@Slf4j
class DefaultLoggingRequestInterceptor implements ClientHttpRequestInterceptor {

  @Override
  ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    log.info("URI: {}", request.getURI())
    log.info("Headers: {}", request.getHeaders())
    return execution.execute(request, body)
  }
}
