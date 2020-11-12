package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.model.release.ReleaseEntityState
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.testutil.WithExceptionResolver
import rocks.metaldetector.butler.web.api.Pagination
import rocks.metaldetector.butler.web.api.ReleaseUpdateRequest
import rocks.metaldetector.butler.web.api.ReleasesRequest
import rocks.metaldetector.butler.web.api.ReleasesRequestPaginated
import rocks.metaldetector.butler.web.api.ReleasesResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat
import java.time.LocalDate

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static rocks.metaldetector.butler.DtoFactory.ReleaseDtoFactory
import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASES
import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASES_UNPAGINATED
import static rocks.metaldetector.butler.config.constants.Endpoints.UPDATE_RELEASE
import static rocks.metaldetector.butler.model.release.ReleaseEntityState.FAULTY

class ReleasesRestControllerTest extends Specification implements WithExceptionResolver {

  static final String ARTIST_NAME = "A1"

  ReleasesRestController underTest = new ReleasesRestController(releaseService: Mock(ReleaseService))
  MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest, exceptionResolver()).build()
  ObjectMapper objectMapper = new ObjectMapper(dateFormat: new SimpleDateFormat("yyyy-MM-dd"))

  void setup() {
    objectMapper.registerModule(new JavaTimeModule())
  }

  def "getAllReleases: should call releases service if no time range is given"() {
    given:
    def artists = [ARTIST_NAME]
    def releasesRequest = new ReleasesRequest(artists: artists)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(artists)
  }

  def "getAllReleases: should return ok if no time range is given"() {
    given:
    def releasesRequest = new ReleasesRequest(artists: [ARTIST_NAME])
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
    def releasesRequest = new ReleasesRequest(artists: [ARTIST_NAME])
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(_) >> expectedReleasesReponse

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse == expectedReleasesReponse
  }

  def "getAllReleases: should call releases service if time range is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequest(artists: artists, dateFrom: from, dateTo: to)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(artists, TimeRange.of(from, to))
  }

  def "getAllReleases: should return ok if time range is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1))
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getAllReleases: should return result from release service if time range is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1))
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

  def "getAllReleases: should call releases service if only 'dateFrom' is given"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequest(artists: artists, dateFrom: from)
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(artists, from) >> []
  }

  def "getAllReleases: should return ok if only 'dateFrom' is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1))
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "getAllReleases: should return result from release service if only 'dateFrom' is given"() {
    given:
    def requestDto = new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1))
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
             new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  @Unroll
  "getAllReleases: bad request should return status bad request"() {
    given:
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()

    where:
    body << ["",
             null,
             new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  def "getPaginatedReleases: valid request without time range should call releases service"() {
    given:
    def artists = [ARTIST_NAME]
    def releasesRequest = new ReleasesRequestPaginated(artists: artists, page: 1, size: 10)
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(releasesRequest.artists, ReleaseEntityState.OK, releasesRequest.page, releasesRequest.size)
  }

  def "getPaginatedReleases: valid request without time range should return ok"() {
    given:
    def releasesRequest = new ReleasesRequestPaginated(artists: [ARTIST_NAME], page: 1, size: 10)
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
    def releasesRequest = new ReleasesRequestPaginated(artists: [ARTIST_NAME], page: 1, size: 10)
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
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequestPaginated(artists: artists, dateFrom: from, dateTo: to, page: 1, size: 10)
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(artists, TimeRange.of(from, to), ReleaseEntityState.OK, requestDto.page, requestDto.size)
  }

  def "getPaginatedReleases: valid request with time range should return ok"() {
    given:
    def requestDto = new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 10)
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
    def requestDto = new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 10)
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
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequestPaginated(artists: artists, dateFrom: from, page: 1, size: 10)
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesSince(artists, from, ReleaseEntityState.OK, requestDto.page, requestDto.size) >> []
  }

  def "getPaginatedReleases: should return ok if only 'dateFrom' is given"() {
    given:
    def requestDto = new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1), page: 1, size: 10)
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
    def requestDto = new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1), page: 1, size: 10)
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

  @Unroll
  "getPaginatedReleases: bad request should return status bad request"() {
    given:
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()

    and:
    0 * underTest.releaseService.findAllUpcomingReleases(_)
    0 * underTest.releaseService.findAllReleasesForTimeRange(*_)

    where:
    body << ["",
             null,
             new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 10),
             new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1),
                                          dateTo: LocalDate.of(2020, 2, 1), page: 0, size: 10),
             new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1),
                                          dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 0),
             new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1),
                                          dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 51),
             new ReleasesRequestPaginated(dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2020, 2, 1),
                                          page: 1, size: 51),
             new ReleasesRequestPaginated(artists: [ARTIST_NAME], dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2019, 1, 1),
                                          page: 1, size: 10)]
  }

  def "updateReleaseState: should call releasesService"() {
    given:
    def body = new ReleaseUpdateRequest(1L, FAULTY)
    def request = put(UPDATE_RELEASE)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.updateReleaseState(body.releaseId, body.state)
  }

  def "updateReleaseState: should return OK"() {
    given:
    def body = new ReleaseUpdateRequest(1L, FAULTY)
    def request = put(UPDATE_RELEASE)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  @Unroll
  "updateReleaseState: should return BAD REQUEST"() {
    given:
    def request = put(UPDATE_RELEASE)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()

    where:
    body << [new ReleaseUpdateRequest(0L, FAULTY),
             new ReleaseUpdateRequest(1L, null)]
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
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 2, 28))
        ]
    )
  }
}
