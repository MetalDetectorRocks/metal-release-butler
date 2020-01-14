package com.metalr2.butler.web.rest

import com.metalr2.butler.config.Endpoints
import com.metalr2.butler.model.TimeRange
import com.metalr2.butler.service.ReleaseService
import com.metalr2.butler.web.dto.ReleaseDto
import com.metalr2.butler.web.dto.ReleasesRequest
import com.metalr2.butler.web.dto.ReleasesResponse
import io.restassured.http.ContentType
import org.junit.jupiter.api.Tag
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static com.metalr2.butler.DtoFactory.ReleaseDtoFactory
import static io.restassured.RestAssured.given

@Tag("integration-test")
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReleasesRestControllerIT extends Specification {

  @LocalServerPort
  private int port

  @SpringBean
  private ReleaseService releaseService = Mock()

  def "test releases endpoint for artists with status ok"() {
    given:
    String requestUri = "http://localhost:" + port + "/metal-release-butler" + Endpoints.RELEASES
    ReleasesRequest requestDto = new ReleasesRequest(artists: ["A1"])
    def request = given().body(requestDto).accept(ContentType.JSON).contentType(ContentType.JSON)

    when:
    def response = request.when().post(requestUri)

    then:
    1 * releaseService.findAllUpcomingReleases(["A1"]) >> getReleaseDtosForTimeRangeTest()

    and:
    response.statusCode() == HttpStatus.OK.value()

    and:
    ReleasesResponse releasesResponse = response.body().as(ReleasesResponse)
    releasesResponse.releases.size() == 2
    releasesResponse.releases.containsAll(getReleaseDtosForTimeRangeTest())
  }

  def "test releases endpoint for artists with time range with status ok"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    String requestUri = "http://localhost:" + port + "/metal-release-butler" + Endpoints.RELEASES
    ReleasesRequest requestDto = new ReleasesRequest(artists: ["A1"],
                                                     dateFrom: from,
                                                     dateTo: to)
    def request = given().body(requestDto).accept(ContentType.JSON).contentType(ContentType.JSON)

    when:
    def response = request.when().post(requestUri)

    then:
    1 * releaseService.findAllReleasesForTimeRange(["A1"], TimeRange.of(from, to)) >> [ReleaseDtoFactory.one("A1", LocalDate.of(2020, 1, 31))]

    and:
    response.statusCode() == HttpStatus.OK.value()

    and:
    ReleasesResponse releasesResponse = response.body().as(ReleasesResponse)
    releasesResponse.releases.size() == 1
    releasesResponse.releases.contains(ReleaseDtoFactory.one("A1", LocalDate.of(2020, 1, 31)))
  }

  @Unroll
  def "test releases endpoint for artists with bad requests"() {
    given:
    def from = null
    def to = LocalDate.of(2020, 2, 1)
    String requestUri = "http://localhost:" + port + "/metal-release-butler" + Endpoints.RELEASES
    ReleasesRequest requestDto = new ReleasesRequest(artists: ["A1"],
                                                     dateFrom: from,
                                                     dateTo: to)
    def request = given().body(requestDto).accept(ContentType.JSON).contentType(ContentType.JSON)

    when:
    def response = request.when().post(requestUri)

    then:
    0 * releaseService.findAllUpcomingReleases(_)
    0 * releaseService.findAllReleasesForTimeRange(*_)

    and:
    response.statusCode() == HttpStatus.BAD_REQUEST.value()

    where:
    body << ["",
             new ReleasesRequest(artists: ["A1"], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  private List<ReleaseDto> getReleaseDtosForTimeRangeTest() {
    return [ReleaseDtoFactory.one("A1", LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.one("A1", LocalDate.of(2020, 2, 28))]
  }
}
