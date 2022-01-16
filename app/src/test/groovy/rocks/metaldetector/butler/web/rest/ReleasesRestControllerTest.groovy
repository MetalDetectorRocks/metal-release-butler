package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortHandlerMethodArgumentResolver
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rocks.metaldetector.butler.persistence.domain.TimeRange
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.web.api.Pagination
import rocks.metaldetector.butler.web.api.ReleaseUpdateRequest
import rocks.metaldetector.butler.web.api.ReleasesRequest
import rocks.metaldetector.butler.web.api.ReleasesRequestPaginated
import rocks.metaldetector.butler.web.api.ReleasesResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat
import java.time.LocalDate

import static org.springframework.data.domain.Sort.Direction.ASC
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.FAULTY
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES_UNPAGINATED
import static rocks.metaldetector.butler.testutil.DtoFactory.ReleaseDtoFactory

class ReleasesRestControllerTest extends Specification {

  private static final List<String> ARTISTS = ["a1"]
  private static final Sort DEFAULT_SORTING = Sort.by(ASC, "releaseDate", "artist", "albumTitle")

  ReleasesRestController underTest = new ReleasesRestController(releaseService: Mock(ReleaseService))
  MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest)
      .setCustomArgumentResolvers(new SortHandlerMethodArgumentResolver()).build()
  ObjectMapper objectMapper = new ObjectMapper(dateFormat: new SimpleDateFormat("yyyy-MM-dd"))

  void setup() {
    objectMapper.registerModule(new JavaTimeModule())
  }

  def "getAllReleases: should call releases service if no time range is given"() {
    given:
    def releasesRequest = new ReleasesRequest(artists: ARTISTS)
    def expectedSorting = Sort.by(ASC, "artist")
    def request = post(RELEASES_UNPAGINATED)
        .param("sort", "artist,asc")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(releasesRequest.artists, expectedSorting)
  }

  def "getAllReleases: should use default sorting if none is given"() {
    given:
    def releasesRequest = new ReleasesRequest(artists: ARTISTS)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(_, DEFAULT_SORTING)
  }

  def "getAllReleases: should convert artists to lower case"() {
    given:
    def releasesRequest = new ReleasesRequest(artists: ["A1"])
    def expectedArtists = ["a1"]
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(expectedArtists, _)
  }

  def "getAllReleases: should return ok if no time range is given"() {
    given:
    def releasesRequest = new ReleasesRequest(artists: ARTISTS)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getAllReleases: should return result from release service if no time range is given"() {
    given:
    def expectedReleasesReponse = createReleasesResponse()
    def releasesRequest = new ReleasesRequest(artists: ARTISTS)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(*_) >> expectedReleasesReponse

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse == expectedReleasesReponse
  }

  def "getAllReleases with TimeRange: should call releases service if time range is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: from, dateTo: to)
    def expectedSorting = Sort.by(ASC, "artist")
    def request = post(RELEASES_UNPAGINATED)
        .param("sort", "artist,asc")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(requestDto.artists, TimeRange.of(from, to), expectedSorting)
  }

  def "getAllReleases with TimeRange: should use default sorting if none is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: from, dateTo: to)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(_, _, DEFAULT_SORTING)
  }

  def "getAllReleases with TimeRange: should convert artists to lower case"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def requestDto = new ReleasesRequest(artists: ["A1"], dateFrom: from, dateTo: to)
    def expectedArtists = ["a1"]
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(expectedArtists, _, _)
  }

  def "getAllReleases with TimeRange: should return ok if time range is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1))
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getAllReleases with TimeRange: should return result from release service if time range is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1))
    def expectedReleasesResponse = createReleasesResponse()
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))
    underTest.releaseService.findAllReleasesForTimeRange(*_) >> expectedReleasesResponse

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse == expectedReleasesResponse
  }

  def "getAllReleases from: should call releases service if only 'dateFrom' is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: from)
    def expectedSorting = Sort.by(ASC, "artist")
    def request = post(RELEASES_UNPAGINATED)
        .param("sort", "artist,asc")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(requestDto.artists, from, expectedSorting) >> []
  }

  def "getAllReleases from: should use default sorting if none is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: from)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(_, _, DEFAULT_SORTING) >> []
  }

  def "getAllReleases from: should convert artists to lower case"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def requestDto = new ReleasesRequest(artists: ["A1"], dateFrom: from)
    def expectedArtists = ["a1"]
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(expectedArtists, _, _) >> []
  }

  def "getAllReleases from: should return ok if only 'dateFrom' is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1))
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getAllReleases from: should return result from release service if only 'dateFrom' is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1))
    def expectedReleasesResponse = createReleasesResponse()
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))
    underTest.releaseService.findAllReleasesSince(*_) >> expectedReleasesResponse

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse == expectedReleasesResponse
  }

  @Unroll
  "getAllReleases: bad request should not call releases service"() {
    given:
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    mockMvc.perform(request).andReturn()

    then:
    0 * underTest.releaseService.findAllUpcomingReleases(_)
    0 * underTest.releaseService.findAllReleasesForTimeRange(*_)

    where:
    body << ["",
             null,
             new ReleasesRequest(artists: ARTISTS, dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  def "getPaginatedReleases: valid request without time range should call releases service"() {
    given:
    def releasesRequest = new ReleasesRequestPaginated(artists: ARTISTS, page: 1, size: 10, query: "query")
    def expectedSorting = Sort.by(ASC, "artist")
    def request = post(RELEASES)
        .param("sort", "artist,asc")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(releasesRequest.artists, releasesRequest.query, releasesRequest.page, releasesRequest.size, expectedSorting)
  }

  def "getPaginatedReleases: valid request without time range should call releases service with default sorting if none is given"() {
    given:
    def releasesRequest = new ReleasesRequestPaginated(artists: ARTISTS, page: 1, size: 10, query: "query")
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(_, _, _, _, DEFAULT_SORTING)
  }

  def "getPaginatedReleases: valid request without time range should convert artists to lower case"() {
    given:
    def releasesRequest = new ReleasesRequestPaginated(artists: ["A1"], page: 1, size: 10, query: "query")
    def expectedArtists = ["a1"]
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(expectedArtists, _, _, _, _)
  }

  def "getPaginatedReleases: valid request without time range should return ok"() {
    given:
    def releasesRequest = new ReleasesRequestPaginated(artists: ARTISTS, page: 1, size: 10)
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getPaginatedReleases: valid request without time range should return releases"() {
    given:
    def expectedReleasesResponse = createReleasesResponse()
    def releasesRequest = new ReleasesRequestPaginated(artists: ARTISTS, page: 1, size: 10)
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(*_) >> expectedReleasesResponse

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse == expectedReleasesResponse
  }

  def "getPaginatedReleases: valid request with time range should call releases service"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: from, dateTo: to, page: 1, size: 10, query: "query")
    def expectedSorting = Sort.by(ASC, "artist")
    def request = post(RELEASES)
        .param("sort", "artist,asc")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(requestDto.artists, TimeRange.of(from, to), requestDto.query, requestDto.page, requestDto.size, expectedSorting)
  }

  def "getPaginatedReleases: valid request with time range should call releases service with default sorting if none is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: from, dateTo: to, page: 1, size: 10, query: "query")
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(_, _, _, _, _, DEFAULT_SORTING)
  }

  def "getPaginatedReleases: valid request with time range should convert artists to lower case"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def requestDto = new ReleasesRequestPaginated(artists: ["A1"], dateFrom: from, dateTo: to, page: 1, size: 10, query: "query")
    def expectedArtists = ["a1"]
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(expectedArtists, _, _, _, _, _)
  }

  def "getPaginatedReleases: valid request with time range should return ok"() {
    given:
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 10)
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getPaginatedReleases: valid request with time range should return releases"() {
    given:
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 10)
    def expectedReleasesResponse = createReleasesResponse()
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))
    underTest.releaseService.findAllReleasesForTimeRange(*_) >> expectedReleasesResponse

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse == expectedReleasesResponse
  }

  def "getPaginatedReleases: should call releases service if only 'dateFrom' is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: from, page: 1, size: 10, query: "query")
    def expectedSorting = Sort.by(ASC, "artist")
    def request = post(RELEASES)
        .param("sort", "artist,asc")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(requestDto.artists, from, requestDto.query, requestDto.page, requestDto.size, expectedSorting) >> []
  }

  def "getPaginatedReleases: should call releases service with default sorting if only 'dateFrom' and no sorting is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: from, page: 1, size: 10, query: "query")
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(_, _, _, _, _, DEFAULT_SORTING) >> []
  }

  def "getPaginatedReleases: should convert artists to lower case if only 'dateFrom' and no sorting is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def requestDto = new ReleasesRequestPaginated(artists: ["A1"], dateFrom: from, page: 1, size: 10, query: "query")
    def expectedArtists = ["a1"]
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(expectedArtists, _, _, _, _, _) >> []
  }

  def "getPaginatedReleases: should return ok if only 'dateFrom' is given"() {
    given:
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), page: 1, size: 10)
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getPaginatedReleases: should return result from release service if only 'dateFrom' is given"() {
    given:
    def requestDto = new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), page: 1, size: 10)
    def expectedReleasesResponse = createReleasesResponse()
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))
    underTest.releaseService.findAllReleasesSince(*_) >> expectedReleasesResponse

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse == expectedReleasesResponse
  }

  def "updateReleaseState: should call releasesService"() {
    given:
    def releaseId = 1L
    def body = new ReleaseUpdateRequest(FAULTY)
    def request = put("${RELEASES}/${releaseId}")
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.updateReleaseState(releaseId, body.state)
  }

  def "updateReleaseState: should return OK"() {
    given:
    def body = new ReleaseUpdateRequest(FAULTY)
    def request = put("${RELEASES}/1")
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  private static ReleasesResponse createReleasesResponse() {
    return new ReleasesResponse(
        pagination: new Pagination(
            currentPage: 1,
            size: 10,
            totalReleases: 2,
            totalPages: 1
        ),
        releases: [
            ReleaseDtoFactory.createReleaseDto(ARTISTS[0], LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.createReleaseDto(ARTISTS[0], LocalDate.of(2020, 2, 28))
        ]
    )
  }
}
