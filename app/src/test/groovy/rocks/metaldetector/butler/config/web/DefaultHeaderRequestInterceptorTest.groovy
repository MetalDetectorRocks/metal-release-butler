package rocks.metaldetector.butler.config.web

import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import spock.lang.Specification

import static org.springframework.http.MediaType.APPLICATION_JSON

class DefaultHeaderRequestInterceptorTest extends Specification {

  DefaultHeaderRequestInterceptor underTest = new DefaultHeaderRequestInterceptor(userAgent: "userAgent")

  def "accept header is set"() {
    given:
    def mockRequest = new MockClientHttpRequest()

    when:
    underTest.intercept(mockRequest, new byte[0], Mock(ClientHttpRequestExecution))

    then:
    mockRequest.getHeaders().getAccept() == [APPLICATION_JSON]
  }

  def "user-agent header is set"() {
    given:
    def mockRequest = new MockClientHttpRequest()

    when:
    underTest.intercept(mockRequest, new byte[0], Mock(ClientHttpRequestExecution))

    then:
    mockRequest.getHeaders().get("User-Agent") == [underTest.userAgent]
  }
}
