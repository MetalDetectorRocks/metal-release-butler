package rocks.metaldetector.butler.supplier.timeformetal

import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static rocks.metaldetector.butler.supplier.timeformetal.TimeForMetalWebCrawler.getUPCOMING_RELEASES_URL

class TimeForMetalWebCrawlerTest extends Specification {

  TimeForMetalWebCrawler underTest = new TimeForMetalWebCrawler(restTemplate: Mock(RestTemplate))
  ResponseEntity responseMock = Mock(ResponseEntity)

  def "RestTemplate is called with correct URL"() {
    when:
    underTest.requestReleases(1)

    then:
    1 * underTest.restTemplate.getForEntity(UPCOMING_RELEASES_URL, _, _) >> responseMock
  }

  def "RestTemplate is called with correct return type"() {
    when:
    underTest.requestReleases(1)

    then:
    1 * underTest.restTemplate.getForEntity(_, String, _) >> responseMock
  }

  def "RestTemplate is called with page parameter"() {
    given:
    def page = 1

    when:
    underTest.requestReleases(page)

    then:
    1 * underTest.restTemplate.getForEntity(_, _, [page: page]) >> responseMock
  }

  def "ResponseBody is returned"() {
    given:
    underTest.restTemplate.getForEntity(*_) >> responseMock
    def responsePage = "page"

    when:
    def result = underTest.requestReleases(1)

    then:
    1 * responseMock.body >> responsePage

    and:
    result == responsePage
  }

  def "nothing happens on exception and null is returned"() {
    given:
    underTest.restTemplate.getForEntity(*_) >> {throw new Exception()}

    when:
    def result = underTest.requestReleases(1)

    then:
    noExceptionThrown()

    and:
    !result
  }
}
