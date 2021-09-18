package rocks.metaldetector.butler.config.web

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import org.springframework.mock.http.client.MockClientHttpResponse
import spock.lang.Specification

import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.WARN
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.OK

class CustomClientErrorHandlerTest extends Specification {

  static Logger logger = (Logger) LoggerFactory.getLogger(CustomClientErrorHandler.class)
  static ListAppender<ILoggingEvent> listAppender
  CustomClientErrorHandler underTest = new CustomClientErrorHandler()

  def setup() {
    listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)
  }

  def tearDown() {
    logger.detachAppender(listAppender)
  }

  def "hasError returns #expectedResult for #mockResponse.getStatusCode()"() {
    when:
    def result = underTest.hasError(mockResponse)

    then:
    result == expectedResult

    where:
    mockResponse                                                   | expectedResult
    new MockClientHttpResponse(new byte[0], INTERNAL_SERVER_ERROR) | true
    new MockClientHttpResponse(new byte[0], BAD_REQUEST)           | true
    new MockClientHttpResponse(new byte[0], OK)                    | false
  }

  def "handleError logs error for 5xx codes"() {
    when:
    underTest.handleError(new URI("http://internet.com"), GET, new MockClientHttpResponse(new byte[0], INTERNAL_SERVER_ERROR))

    then:
    List<ILoggingEvent> logsList = listAppender.list;

    logsList.size() == 1
    logsList.get(0).getLevel() == ERROR
  }

  def "handleError logs warning for 4xx codes"() {
    when:
    underTest.handleError(new URI("http://internet.com"), GET, new MockClientHttpResponse(new byte[0], BAD_REQUEST))

    then:
    List<ILoggingEvent> logsList = listAppender.list;

    logsList.size() == 1
    logsList.get(0).getLevel() == WARN
  }
}
