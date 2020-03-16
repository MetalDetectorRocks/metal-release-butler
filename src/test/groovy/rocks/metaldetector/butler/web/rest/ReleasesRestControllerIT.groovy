package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.config.Endpoints
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.service.ReleaseServiceImpl
import rocks.metaldetector.butler.testutil.WithIntegrationTestProfile
import rocks.metaldetector.butler.web.dto.ReleaseDto
import rocks.metaldetector.butler.web.dto.ReleasesRequest
import rocks.metaldetector.butler.web.dto.ReleasesResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat
import java.time.LocalDate

import static org.mockito.Mockito.reset
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.DtoFactory.ReleaseDtoFactory

@SpringBootTest
@AutoConfigureMockMvc
class ReleasesRestControllerIT extends Specification implements WithIntegrationTestProfile {

  static final String ARTIST_NAME = "A1"

  @SpringBean
  ReleaseServiceImpl releaseService = Mock()

  @Autowired
  MockMvc mockMvc

  ObjectMapper objectMapper

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapper(dateFormat: new SimpleDateFormat("yyyy-MM-dd"))
    objectMapper.registerModule(new JavaTimeModule())
  }

  @AfterEach
  void tearDown() {
    reset(releaseService)
  }

  def "test releases endpoint for artists with status ok"() {
    given:
    def artists = [ARTIST_NAME]
    def expectedReleases = getReleaseDtosForTimeRangeTest()
    def releasesRequest = new ReleasesRequest(artists: artists)
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    1 * releaseService.findAllUpcomingReleases(artists) >> expectedReleases

    and:
    result.response.status == HttpStatus.OK.value()

    and:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse.class)
    releasesResponse.releases.size() == 2
    releasesResponse.releases == expectedReleases
  }

  def "test releases endpoint for artists with time range with status ok"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequest(artists: artists, dateFrom: from, dateTo: to)
    def expectedReleases = [ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31))]
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    1 * releaseService.findAllReleasesForTimeRange(artists, TimeRange.of(from, to)) >> expectedReleases

    and:
    result.response.status == HttpStatus.OK.value()

    and:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse.class)
    releasesResponse.releases == expectedReleases
  }

  @Unroll
  "test releases endpoint for artists with bad requests"() {
    given:
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    0 * releaseService.findAllUpcomingReleases(_)
    0 * releaseService.findAllReleasesForTimeRange(*_)

    and:
    result.response.status == HttpStatus.BAD_REQUEST.value()

    where:
    body << ["",
             new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  private static List<ReleaseDto> getReleaseDtosForTimeRangeTest() {
    return [ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 2, 28))]
  }
}
