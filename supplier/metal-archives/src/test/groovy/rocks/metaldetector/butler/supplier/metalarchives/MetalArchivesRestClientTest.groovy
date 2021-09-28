package rocks.metaldetector.butler.supplier.metalarchives

import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations
import spock.lang.Specification

import static rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient.MAX_ATTEMPTS
import static rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient.UPCOMING_RELEASES_URL

class MetalArchivesRestClientTest extends Specification {

  MetalArchivesRestClient underTest = new MetalArchivesRestClient(restOperations: Mock(RestOperations))

  def "RestTemplate is called with correct URL"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restOperations.getForEntity(UPCOMING_RELEASES_URL, _, _) >> ResponseEntity.ok(new MetalArchivesReleasesResponse(totalRecords: 0, data: []))
  }

  def "RestTemplate is called with correct response type"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restOperations.getForEntity(_, MetalArchivesReleasesResponse, _) >> ResponseEntity.ok(new MetalArchivesReleasesResponse(totalRecords: 0, data: []))
  }

  def "RestTemplate is called with correct URL parameter range, starting with 0"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restOperations.getForEntity(_, _, 0) >> ResponseEntity.ok(new MetalArchivesReleasesResponse(totalRecords: 0, data: []))
  }

  def "On faulty response the same range is requested again"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restOperations.getForEntity(_, _, 0) >> {throw new RuntimeException("exception")}

    then:
    1 * underTest.restOperations.getForEntity(_, _, 0) >> ResponseEntity.ok(new MetalArchivesReleasesResponse(totalRecords: 0, data: []))
  }

  def "The maximum number of attempts is 5"() {
    when:
    underTest.requestReleases()

    then:
    MAX_ATTEMPTS * underTest.restOperations.getForEntity(_, _, 0) >> {throw new RuntimeException("exception")}

    then:
    0 * underTest.restOperations.getForEntity(*_)
  }

  def "If the total number of records does not exceed the current range, the client is not called again"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restOperations.getForEntity(_, _, 0) >> ResponseEntity.ok(new MetalArchivesReleasesResponse(totalRecords: 99, data: []))

    then:
    0 * underTest.restOperations.getForEntity(*_)
  }

  def "If the total number of records exceeds the current range, the client is called with the next bigger range"() {
    when:
    underTest.requestReleases()

    then:
    1 * underTest.restOperations.getForEntity(_, _, 0) >> ResponseEntity.ok(new MetalArchivesReleasesResponse(totalRecords: 150, data: []))

    then:
    1 * underTest.restOperations.getForEntity(_, _, 100) >> ResponseEntity.ok(new MetalArchivesReleasesResponse(totalRecords: 50, data: []))
  }

  def "The response data is returned"() {
    given:
    def metalArchivesResponse = new MetalArchivesReleasesResponse(totalRecords: 0, data: [["Test1"], ["Test2"]])
    underTest.restOperations.getForEntity(*_) >> ResponseEntity.ok(metalArchivesResponse)

    when:
    def response = underTest.requestReleases()

    then:
    response == metalArchivesResponse.data
  }
}
