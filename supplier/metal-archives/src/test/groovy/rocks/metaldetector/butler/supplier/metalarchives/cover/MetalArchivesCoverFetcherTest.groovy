package rocks.metaldetector.butler.supplier.metalarchives.cover

import groovyx.net.http.HttpBuilder
import org.jsoup.Jsoup
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.ClassPathResource
import rocks.metaldetector.butler.supplier.infrastructure.cover.HttpBuilderFunction
import spock.lang.Specification

class MetalArchivesCoverFetcherTest extends Specification {

  MetalArchivesCoverFetcher underTest = new MetalArchivesCoverFetcher(
      httpBuilderFunction: Mock(HttpBuilderFunction),
      eventPublisher: Mock(ApplicationEventPublisher)
  )
  HttpBuilder mockHttpBuilder = Mock(HttpBuilder)
  String requestUrl = "http://www.internet.de"

  def "httpBuilderFunction is called to get new instance of HttpBuilder"() {
    when:
    underTest.fetchCoverUrl(requestUrl)

    then:
    1 * underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder
  }

  def "httpBuilder is called to get release page"() {
    given:
    underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder

    when:
    underTest.fetchCoverUrl(requestUrl)

    then:
    1 * mockHttpBuilder.get()
  }

  def "URL containing release cover link is returned"() {
    given:
    def mockReleasePageResource = new ClassPathResource("mock-release-page-metal-archives.html")
    def mockReleasePage = Jsoup.parse(mockReleasePageResource.inputStream, "UTF-8", "mock-release-page-metal-archives.html")
    underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder
    mockHttpBuilder.get() >> mockReleasePage

    when:
    def result = underTest.fetchCoverUrl(requestUrl)

    then:
    result == "https://www.i-am-a-cover.com"
  }

  def "if getting the release page fails 5 times null is returned"() {
    given:
    underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder

    when:
    def result = underTest.fetchCoverUrl(requestUrl)

    then:
    5 * mockHttpBuilder.get(*_) >> { throw new RuntimeException() }

    and:
    !result

    and:
    noExceptionThrown()
  }

  def "should not retry getting the release page on status code 404"() {
    given:
    underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder

    when:
    def result = underTest.fetchCoverUrl(requestUrl)

    then:
    1 * mockHttpBuilder.get(*_) >> { throw new RuntimeException("(status code: 404, reason phrase: Not Found)") }

    and:
    !result

    and:
    noExceptionThrown()
  }

  def "should publish ReleaseEntityDeleteRequest on status code 404 when getting the release page"() {
    given:
    underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder
    mockHttpBuilder.get(*_) >> { throw new RuntimeException("(status code: 404, reason phrase: Not Found)") }

    when:
    underTest.fetchCoverUrl(requestUrl)

    then:
    1 * underTest.eventPublisher.publishEvent({ args ->
      args.source == underTest
      args.releaseDetailsUrl == requestUrl
    })
  }
}
