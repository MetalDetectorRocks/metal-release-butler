package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.support.StaticApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.accept.ContentNegotiationManager
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import rocks.metaldetector.butler.config.constants.Endpoints
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.service.ReleaseService
import rocks.metaldetector.butler.web.dto.ReleaseDto
import rocks.metaldetector.butler.web.dto.ReleaseImportResponse
import rocks.metaldetector.butler.web.dto.ReleasesRequest
import rocks.metaldetector.butler.web.dto.ReleasesRequestPaginated
import rocks.metaldetector.butler.web.dto.ReleasesResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat
import java.time.LocalDate

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.DtoFactory.ReleaseDtoFactory

class ReleasesRestControllerTest extends Specification {

  static final String ARTIST_NAME = "A1"

  ReleasesRestController underTest

  MockMvc mockMvc

  ObjectMapper objectMapper

  void setup() {
    underTest = new ReleasesRestController(releaseService: Mock(ReleaseService))
    mockMvc = MockMvcBuilders.standaloneSetup(underTest, exceptionResolver()).build()

    objectMapper = new ObjectMapper(dateFormat: new SimpleDateFormat("yyyy-MM-dd"))
    objectMapper.registerModule(new JavaTimeModule())
  }

  def "Requesting unpaginated releases endpoint with valid request should call releases service"() {
    given:
    def artists = [ARTIST_NAME]
    def releasesRequest = new ReleasesRequest(artists: artists)
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(artists)
  }

  def "Requesting unpaginated releases endpoint with valid request should return ok"() {
    given:
    def releasesRequest = new ReleasesRequest(artists: [ARTIST_NAME])
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(_) >> getReleaseDtosForTimeRangeTest()

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Requesting unpaginated releases endpoint with valid request should return releases"() {
    given:
    def expectedReleases = getReleaseDtosForTimeRangeTest()
    def releasesRequest = new ReleasesRequest(artists: [ARTIST_NAME])
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(_) >> expectedReleases

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse.releases.size() == 2
    releasesResponse.releases == expectedReleases
  }

  def "Requesting unpaginated releases endpoint with valid request with time range should call releases service"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequest(artists: artists, dateFrom: from, dateTo: to)
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(artists, TimeRange.of(from, to))
  }

  def "Requesting unpaginated releases endpoint with valid request with time range should return ok"() {
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
    underTest.releaseService.findAllReleasesForTimeRange(*_) >> expectedReleases

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Requesting unpaginated releases endpoint with valid request with time range should return releases"() {
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
    underTest.releaseService.findAllReleasesForTimeRange(artists, TimeRange.of(from, to)) >> expectedReleases

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse.releases == expectedReleases
  }

  @Unroll
  "Requesting unpaginated releases with bad request should not call releases service"() {
    given:
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    mockMvc.perform(request).andReturn()

    then:
    0 * underTest.releaseService.findAllUpcomingReleases(_)
    0 * underTest.releaseService.findAllReleasesForTimeRange(*_)

    where:
    body << ["",
             new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  @Unroll
  "Requesting unpaginated releases with bad request should return bad request"() {
    given:
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()

    where:
    body << ["",
             new ReleasesRequest(artists: [ARTIST_NAME], dateFrom: null, dateTo: LocalDate.of(2020, 2, 1))]
  }

  def "Requesting releases endpoint with valid request should call releases service"() {
    given:
    def artists = [ARTIST_NAME]
    def releasesRequest = new ReleasesRequestPaginated(artists: artists, page: 1, size: 10)
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllUpcomingReleases(releasesRequest.artists, releasesRequest.page, releasesRequest.size)

    and:
    1 * underTest.releaseService.totalCountAllUpcomingReleases(releasesRequest.artists)
  }

  def "Requesting releases endpoint with valid request should return ok"() {
    given:
    def expectedReleases = getReleaseDtosForTimeRangeTest()
    def releasesRequest = new ReleasesRequestPaginated(artists: [ARTIST_NAME], page: 1, size: 10)
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(*_) >> expectedReleases

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Requesting releases endpoint with valid request should return releases"() {
    given:
    def expectedReleases = getReleaseDtosForTimeRangeTest()
    def releasesRequest = new ReleasesRequestPaginated(artists: [ARTIST_NAME], page: 1, size: 10)
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(*_) >> expectedReleases
    underTest.releaseService.totalCountAllUpcomingReleases(releasesRequest.artists) >> expectedReleases.size()

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse.releases.size() == 2
    releasesResponse.releases == expectedReleases
  }

  def "Requesting releases endpoint with valid request should return requested pagination"() {
    given:
    def expectedReleases = getReleaseDtosForTimeRangeTest()
    def releasesRequest = new ReleasesRequestPaginated(artists: [ARTIST_NAME], page: 1, size: 10)
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(*_) >> expectedReleases

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse.currentPage == releasesRequest.page
    releasesResponse.size == releasesRequest.size
  }

  def "Total pages are calculates correctly"() {
    given:
    def expectedPages = 3
    def expectedReleases = getReleaseDtosForPaginationTest()
    def releasesRequest = new ReleasesRequestPaginated(artists: [ARTIST_NAME], page: 1, size: 2)
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(releasesRequest))
    underTest.releaseService.findAllUpcomingReleases(*_) >> expectedReleases
    underTest.releaseService.totalCountAllUpcomingReleases(_) >> expectedReleases.size()

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse.totalReleases == expectedReleases.size()
    releasesResponse.totalPages == expectedPages
  }

  def "Requesting releases endpoint with valid request with time range should call releases service"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequestPaginated(artists: artists, dateFrom: from, dateTo: to, page: 1, size: 10)
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.findAllReleasesForTimeRange(artists, TimeRange.of(from, to), requestDto.page, requestDto.size)

    and:
    1 * underTest.releaseService.totalCountAllReleasesForTimeRange(artists, TimeRange.of(from, to))
  }

  def "Requesting releases endpoint with valid request with time range should return ok"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequestPaginated(artists: artists, dateFrom: from, dateTo: to, page: 1, size: 10)
    def expectedReleases = [ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31))]
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))
    underTest.releaseService.findAllReleasesForTimeRange(*_) >> expectedReleases

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Requesting releases endpoint with valid request with time range should return releases"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = [ARTIST_NAME]
    def requestDto = new ReleasesRequestPaginated(artists: artists, dateFrom: from, dateTo: to, page: 1, size: 10)
    def expectedReleases = [ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31))]
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto))
    underTest.releaseService.findAllReleasesForTimeRange(*_) >> expectedReleases

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleasesResponse releasesResponse = objectMapper.readValue(result.response.getContentAsString(), ReleasesResponse)
    releasesResponse.releases == expectedReleases
  }

  @Unroll
  "Requesting releases with bad request should return bad request"() {
    given:
    def request = post(Endpoints.RELEASES)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
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

  def "Requesting import endpoint with correct action should return ok"() {
    given:
    def request = get(Endpoints.RELEASES)
        .accept(MediaType.APPLICATION_JSON)
        .param("action", "import")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Requesting import endpoint with correct action should call releases service"() {
    given:
    def request = get(Endpoints.RELEASES)
        .accept(MediaType.APPLICATION_JSON)
        .param("action", "import")

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.releaseService.importFromExternalSource()
  }

  def "Requesting import endpoint with correct action should return import result"() {
    given:
    def request = get(Endpoints.RELEASES)
        .accept(MediaType.APPLICATION_JSON)
        .param("action", "import")
    def importResultDto = new ReleaseImportResponse(totalCountRequested: 666, totalCountImported: 666)
    underTest.releaseService.importFromExternalSource() >> importResultDto

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ReleaseImportResponse importResponse = objectMapper.readValue(result.response.getContentAsString(), ReleaseImportResponse)
    importResponse.totalCountRequested == importResultDto.totalCountRequested

    and:
    importResponse.totalCountImported == importResultDto.totalCountImported
  }

  def "Requesting import endpoint with any other action should return bad request"() {
    given:
    def request = get(Endpoints.RELEASES)
        .accept(MediaType.APPLICATION_JSON)
        .param("action", "download")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()
  }

  def "Requesting import endpoint with any other action should not call releases service"() {
    given:
    def request = get(Endpoints.RELEASES)
        .accept(MediaType.APPLICATION_JSON)
        .param("action", "download")

    when:
    mockMvc.perform(request).andReturn()

    then:
    0 * underTest.releaseService.importFromExternalSource()
  }

  private static List<ReleaseDto> getReleaseDtosForTimeRangeTest() {
    return [ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 2, 28))]
  }

  private static List<ReleaseDto> getReleaseDtosForPaginationTest() {
    return [ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 2, 28)),
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 3, 31)),
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 4, 28)),
            ReleaseDtoFactory.createReleaseDto(ARTIST_NAME, LocalDate.of(2020, 5, 28))]
  }

  private HandlerExceptionResolver exceptionResolver() {
    def applicationContext = new StaticApplicationContext()
    applicationContext.registerSingleton("exceptionHandler", RestExceptionHandler)

    def webMvcConfigurationSupport = new WebMvcConfigurationSupport()
    webMvcConfigurationSupport.setApplicationContext(applicationContext)

    return webMvcConfigurationSupport.handlerExceptionResolver(new ContentNegotiationManager())
  }
}
