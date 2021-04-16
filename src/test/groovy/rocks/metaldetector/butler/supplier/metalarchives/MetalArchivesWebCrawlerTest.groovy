package rocks.metaldetector.butler.supplier.metalarchives

import org.jsoup.Jsoup
import org.jsoup.helper.HttpConnection
import org.jsoup.nodes.Document
import spock.lang.Specification

import static rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesWebCrawler.REST_ENDPOINT

class MetalArchivesWebCrawlerTest extends Specification {

  MetalArchivesWebCrawler underTest = new MetalArchivesWebCrawler()

  void setup() {
    GroovyMock(Jsoup, global: true)
  }

  def "jsoup calls correct url"() {
    given:
    def releaseId = "666"
    def expectedUrl = REST_ENDPOINT.replaceAll("releaseId", releaseId)

    when:
    underTest.requestOtherReleases(releaseId)

    then:
    1 * Jsoup.connect(expectedUrl) >> new HttpConnection()
  }

  def "GET call is made"() {
    given:
    def mockConnection = Mock(HttpConnection)
    Jsoup.connect(*_) >> mockConnection

    when:
    underTest.requestOtherReleases("666")

    then:
    1 * mockConnection.get()
  }

  def "document is returned"() {
    given:
    def mockConnection = Mock(HttpConnection)
    def document = new Document("")
    Jsoup.connect(*_) >> mockConnection
    mockConnection.get() >> document

    when:
    def result = underTest.requestOtherReleases("666")

    then:
    result == document
  }

  def "exceptions on GET are caught"() {
    given:
    def mockConnection = Mock(HttpConnection)
    def document = new Document("")
    Jsoup.connect(*_) >> mockConnection
    mockConnection.get() >> { throw new IOException() }

    when:
    underTest.requestOtherReleases("666")

    then:
    noExceptionThrown()
  }
}
