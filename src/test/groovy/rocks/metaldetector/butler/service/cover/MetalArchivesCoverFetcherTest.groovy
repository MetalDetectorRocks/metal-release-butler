package rocks.metaldetector.butler.service.cover

import groovy.xml.XmlSlurper
import groovyx.net.http.HTTPBuilder
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

class MetalArchivesCoverFetcherTest extends Specification {

  MetalArchivesCoverFetcher underTest = new MetalArchivesCoverFetcher(httpBuilderFunction: Mock(HTTPBuilderFunction))
  HTTPBuilder mockHttpBuilder = Mock(HTTPBuilder)
  URL requestUrl = new URL("http://www.internet.de")

  def "httpBuilderFunction is called to get new instance of HTTPBuilder"() {
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
    1 * mockHttpBuilder.get(*_)
  }

  def "URL containing release cover link is returned"() {
    given:
    def mockReleasePageResource = new ClassPathResource("mock-release-page.html")
    def mockReleasePage = new XmlSlurper().parse(mockReleasePageResource.inputStream)
    underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder
    mockHttpBuilder.get(*_) >> mockReleasePage

    when:
    URL result = underTest.fetchCoverUrl(requestUrl)

    then:
    result.toURI().toString() == "https://www.i-am-a-cover.com"
  }

  def "if getting the release page fails 5 times null is returned"() {
    given:
    underTest.httpBuilderFunction.apply(requestUrl) >> mockHttpBuilder

    when:
    def result = underTest.fetchCoverUrl(requestUrl)

    then:
    5 * mockHttpBuilder.get(*_) >> {throw new RuntimeException()}

    and:
    !result

    and:
    noExceptionThrown()
  }
}
