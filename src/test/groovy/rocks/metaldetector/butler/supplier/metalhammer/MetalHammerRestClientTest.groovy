package rocks.metaldetector.butler.supplier.metalhammer

import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static rocks.metaldetector.butler.supplier.metalhammer.MetalHammerRestClient.UPCOMING_RELEASES_URL

class MetalHammerRestClientTest extends Specification {

  MetalHammerRestClient underTest = new MetalHammerRestClient(restTemplate: Mock(RestTemplate))
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
}
