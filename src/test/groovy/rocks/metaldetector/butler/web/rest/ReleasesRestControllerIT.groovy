package rocks.metaldetector.butler.web.rest

import io.restassured.http.ContentType
import org.junit.jupiter.api.Tag
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import rocks.metaldetector.butler.DtoFactory
import rocks.metaldetector.butler.config.Endpoints
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.service.ReleaseService
import rocks.metaldetector.butler.web.dto.ReleaseDto
import rocks.metaldetector.butler.web.dto.ReleasesRequest
import rocks.metaldetector.butler.web.dto.ReleasesResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static io.restassured.RestAssured.given

@Tag("integration-test")
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReleasesRestControllerIT extends Specification {

  static final String ARTIST_NAME = "A1"

  @LocalServerPort
  private int port

  @SpringBean
  private ReleaseService releaseService = Mock()

  def "test releases endpoint for artists with status ok"() {
    given:
    String requestUri = "http://localhost:" + port + "/metal-release-butler" + Endpoints.RELEASES + Endpoints.UNPAGINATED
    ReleasesRequest requestDto = new ReleasesRequest(artists: [ARTIST_NAME])
    def request = given().body(requestDto).accept(ContentType.JSON).contentType(ContentType.JSON)

    when:
    def response = request.when().post(requestUri)

    then:
    1 * releaseService.findAllUpcomingReleases([ARTIST_NAME]) >> getReleaseDtosForTimeRangeTest()

    and:
    response.statusCode() == HttpStatus.OK.value()

    and:
    ReleasesResponse releasesResponse = response.body().as(ReleasesResponse)
    releasesResponse.releases.size() == 2
    releasesResponse.releases == getReleaseDtosForTimeRangeTest()
  }

  def "test releases endpoint for artists with time range with status ok"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    String requestUri = "http://localhost:" + port + "/metal-release-butler" + Endpoints.RELEASES + Endpoints.UNPAGINATED
    ReleasesRequest requestDto = new ReleasesRequest(artists: [ARTIST_NAME],
                                                     dateFrom: from,
                                                     dateTo: to)
    def request = given().body(requestDto).accept(ContentType.JSON).contentType(ContentType.JSON)

    when:
    def response = request.when().post(requestUri)

    then:
    1 * releaseService.findAllReleasesForTimeRange([ARTIST_NAME], TimeRange.of(from, to)) >> [DtoFactory.ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31))]

    and:
    response.statusCode() == HttpStatus.OK.value()

    and:
    ReleasesResponse releasesResponse = response.body().as(ReleasesResponse)
    releasesResponse.releases == [DtoFactory.ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31))]
  }

  @Unroll
  def "test releases endpoint for artists with bad requests"() {
    given:
    String requestUri = "http://localhost:" + port + "/metal-release-butler" + Endpoints.RELEASES + Endpoints.UNPAGINATED
    def request = given().body(body).accept(ContentType.JSON).contentType(ContentType.JSON)

    when:
    def response = request.when().post(requestUri)

    then:
    0 * releaseService.findAllUpcomingReleases(_)
    0 * releaseService.findAllReleasesForTimeRange(*_)

    and:
    response.statusCode() == HttpStatus.BAD_REQUEST.value()

    where:
    body << ["",
             new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  private static List<ReleaseDto> getReleaseDtosForTimeRangeTest() {
    return [DtoFactory.ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31)),
            DtoFactory.ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 2, 28))]
  }
}
