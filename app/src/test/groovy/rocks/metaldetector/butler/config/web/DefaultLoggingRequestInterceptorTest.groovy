package rocks.metaldetector.butler.config.web

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.util.LinkedMultiValueMap
import spock.lang.Specification

import static ch.qos.logback.classic.Level.INFO
import static org.springframework.http.HttpMethod.GET

class DefaultLoggingRequestInterceptorTest extends Specification {

  static Logger logger = (Logger) LoggerFactory.getLogger(DefaultLoggingRequestInterceptor.class)
  static ListAppender<ILoggingEvent> listAppender
  DefaultLoggingRequestInterceptor underTest = new DefaultLoggingRequestInterceptor()

  def setup() {
    listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)
  }

  def tearDown() {
    logger.detachAppender(listAppender)
  }

  def "two info log entries are made"() {
    when:
    underTest.intercept(new MockClientHttpRequest(), new byte[0], Mock(ClientHttpRequestExecution))

    then:
    List<ILoggingEvent> logsList = listAppender.list
    logsList.size() == 2

    and:
    logsList.get(0).getLevel() == INFO
    logsList.get(1).getLevel() == INFO
  }

  def "uri is logged"() {
    given:
    def uri = "http://internet.com"
    def mockRequest = new MockClientHttpRequest(GET, new URI(uri))

    when:
    underTest.intercept(mockRequest, new byte[0], Mock(ClientHttpRequestExecution))

    then:
    List<ILoggingEvent> logsList = listAppender.list
    logsList.get(0).formattedMessage == "URI: $uri"
  }

  def "headers are logged"() {
    given:
    def mockRequest = new MockClientHttpRequest()
    mockRequest.headers.addAll(new LinkedMultiValueMap<String, String>(["accept": ["application/json"]]))

    when:
    underTest.intercept(mockRequest, new byte[0], Mock(ClientHttpRequestExecution))

    then:
    List<ILoggingEvent> logsList = listAppender.list
    logsList.get(1).formattedMessage == "Headers: ${mockRequest.headers.toString()}"
  }
}
