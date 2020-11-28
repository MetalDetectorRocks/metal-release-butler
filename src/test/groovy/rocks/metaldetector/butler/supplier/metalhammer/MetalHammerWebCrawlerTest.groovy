package rocks.metaldetector.butler.supplier.metalhammer

import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static rocks.metaldetector.butler.supplier.metalhammer.MetalHammerWebCrawler.UPCOMING_RELEASES_URL

class MetalHammerWebCrawlerTest extends Specification {

  MetalHammerWebCrawler underTest = new MetalHammerWebCrawler(restTemplate: Mock(RestTemplate))
  ResponseEntity responseMock = Mock(ResponseEntity)

  def "RestTemplate is called with correct URL"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restTemplate.getForEntity(UPCOMING_RELEASES_URL, _) >> responseMock
  }

  def "RestTemplate is called with correct return type"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restTemplate.getForEntity(_, String) >> responseMock
  }

  def "ResponseBody is returned"() {
    given:
    underTest.restTemplate.getForEntity(*_) >> responseMock
    def responsePage = "page"

    when:
    def result = underTest.requestReleases()

    then:
    1 * responseMock.body >> responsePage

    and:
    result == responsePage
  }

  def "nothing happens on exception and null is returned"() {
    given:
    underTest.restTemplate.getForEntity(*_) >> {throw new Exception()}

    when:
    def result = underTest.requestReleases()

    then:
    noExceptionThrown()

    and:
    !result
  }
}
