package rocks.metaldetector.butler.config.web

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

import static org.springframework.http.MediaType.APPLICATION_JSON

class DefaultHeaderRequestInterceptor implements ClientHttpRequestInterceptor {

  String userAgent

  @Override
  ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().setAccept([APPLICATION_JSON])
    request.getHeaders().set("User-Agent", userAgent)
    return execution.execute(request, body)
  }
}
