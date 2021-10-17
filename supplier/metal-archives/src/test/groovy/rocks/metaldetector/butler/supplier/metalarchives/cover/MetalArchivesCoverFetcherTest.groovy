package rocks.metaldetector.butler.supplier.metalarchives.cover

import org.jsoup.Jsoup
import org.jsoup.helper.HttpConnection
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

class MetalArchivesCoverFetcherTest extends Specification {

  MetalArchivesCoverFetcher underTest = new MetalArchivesCoverFetcher(
      eventPublisher: Mock(ApplicationEventPublisher)
  )
  String requestUrl = "http://www.internet.de"

  void setup() {
    GroovySpy(Jsoup, global: true)
  }

  def "jsoup calls correct url"() {
    when:
    underTest.fetchCoverUrl(requestUrl)

    then:
    1 * Jsoup.connect(requestUrl) >> Mock(HttpConnection)
  }

  def "GET call is made"() {
    given:
    def mockConnection = Mock(HttpConnection)
    Jsoup.connect(*_) >> mockConnection

    when:
    underTest.fetchCoverUrl(requestUrl)

    then:
    1 * mockConnection.get()
  }

  def "URL containing release cover link is returned"() {
    given:
    def mockReleasePageResource = new ClassPathResource("mock-release-page-metal-archives.html")
    def mockReleasePage = Jsoup.parse(mockReleasePageResource.inputStream, "UTF-8", "mock-release-page-metal-archives.html")
    def mockConnection = Mock(HttpConnection)
    Jsoup.connect(*_) >> mockConnection
    mockConnection.get() >> mockReleasePage

    when:
    def result = underTest.fetchCoverUrl(requestUrl)

    then:
    result == "https://www.i-am-a-cover.com"
  }

  def "if getting the release page fails 5 times null is returned"() {
    given:
    def mockConnection = Mock(HttpConnection)
    Jsoup.connect(*_) >> mockConnection

    when:
    def result = underTest.fetchCoverUrl(requestUrl)

    then:
    5 * mockConnection.get(*_) >> { throw new RuntimeException() }

    and:
    !result

    and:
    noExceptionThrown()
  }

  def "should not retry getting the release page on status code 404"() {
    given:
    def mockConnection = Mock(HttpConnection)
    Jsoup.connect(*_) >> mockConnection

    when:
    def result = underTest.fetchCoverUrl(requestUrl)

    then:
    1 * mockConnection.get(*_) >> { throw new RuntimeException("(status code: 404, reason phrase: Not Found)") }

    and:
    !result

    and:
    noExceptionThrown()
  }

  def "should publish ReleaseEntityDeleteRequest on status code 404 when getting the release page"() {
    given:
    def mockConnection = Mock(HttpConnection)
    Jsoup.connect(*_) >> mockConnection
    mockConnection.get(*_) >> { throw new RuntimeException("(status code: 404, reason phrase: Not Found)") }

    when:
    underTest.fetchCoverUrl(requestUrl)

    then:
    1 * underTest.eventPublisher.publishEvent({ args ->
      args.source == underTest
      args.releaseDetailsUrl == requestUrl
    })
  }
}
