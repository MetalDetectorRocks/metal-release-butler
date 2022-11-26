package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.config.web.ResourceNotFoundException
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import rocks.metaldetector.butler.web.api.ReleaseUpdateRequest
import rocks.metaldetector.butler.web.api.ReleasesRequest
import rocks.metaldetector.butler.web.api.ReleasesRequestPaginated
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.FAULTY
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES_UNPAGINATED
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.UPDATE_RELEASE

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class ReleasesRestControllerIntegrationTest extends Specification implements WithIntegrationTestConfig {

  private final Jwt RELEASES_READ_JWT = createTokenWithScope("releases-read")
  private final Jwt RELEASES_READ_ALL_JWT = createTokenWithScope("releases-read-all")
  private final Jwt RELEASES_WRITE_JWT = createTokenWithScope("releases-write")
  private static final List<String> ARTISTS = ["a1"]

  @Autowired
  MockMvc mockMvc

  @Autowired
  ObjectMapper objectMapper

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  @SpringBean
  ReleaseService releaseService = Mock(ReleaseService)

  @Unroll
  "getAllReleases: faulty request should return status UNPROCESSABLE ENTITY"() {
    given:
    jwtDecoder.decode(*_) >> RELEASES_READ_ALL_JWT
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .header("Authorization", "Bearer $RELEASES_READ_ALL_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == UNPROCESSABLE_ENTITY.value()

    where:
    body << [new ReleasesRequest(artists: ARTISTS, dateFrom: null, dateTo: LocalDate.of(2020, 2, 1)),
             new ReleasesRequest(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2019, 1, 1))]
  }

  @Unroll
  "getAllReleases: missing request body should return status BAD REQUEST"() {
    given:
    jwtDecoder.decode(*_) >> RELEASES_READ_ALL_JWT
    def request = post(RELEASES_UNPAGINATED)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .header("Authorization", "Bearer $RELEASES_READ_ALL_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()

    where:
    body << [null, ""]
  }

  @Unroll
  "getPaginatedReleases: faulty request should return status UNPROCESSABLE ENTITY"() {
    given:
    jwtDecoder.decode(*_) >> RELEASES_READ_JWT
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .header("Authorization", "Bearer $RELEASES_READ_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == UNPROCESSABLE_ENTITY.value()

    where:
    body << [new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: null, dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 10),
             new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1),
                                          dateTo: LocalDate.of(2020, 2, 1), page: 0, size: 10),
             new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1),
                                          dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 0),
             new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1),
                                          dateTo: LocalDate.of(2020, 2, 1), page: 1, size: 51),
             new ReleasesRequestPaginated(artists: ARTISTS, dateFrom: LocalDate.of(2020, 1, 1), dateTo: LocalDate.of(2019, 1, 1),
                                          page: 1, size: 10)]
  }

  @Unroll
  "getPaginatedReleases: missing request body should return status BAD REQUEST"() {
    given:
    jwtDecoder.decode(*_) >> RELEASES_READ_JWT
    def request = post(RELEASES)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .header("Authorization", "Bearer $RELEASES_READ_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()

    where:
    body << [null, ""]
  }

  def "updateReleaseState: should return UNPROCESSABLE ENTITY for missing state"() {
    given:
    jwtDecoder.decode(*_) >> RELEASES_WRITE_JWT
    def request = put(UPDATE_RELEASE, 1)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new ReleaseUpdateRequest(state: null)))
        .header("Authorization", "Bearer $RELEASES_WRITE_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == UNPROCESSABLE_ENTITY.value()
  }

  def "updateReleaseState: should return NOT FOUND for wrong release id"() {
    given:
    jwtDecoder.decode(*_) >> RELEASES_WRITE_JWT
    def request = put(UPDATE_RELEASE, 0)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new ReleaseUpdateRequest(FAULTY)))
        .header("Authorization", "Bearer $RELEASES_WRITE_JWT.tokenValue")
    releaseService.updateReleaseState(*_) >> { throw new ResourceNotFoundException("not found") }

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == NOT_FOUND.value()
  }

  @Unroll
  "updateReleaseState: should return BAD REQUEST for missing request body"() {
    given:
    jwtDecoder.decode(*_) >> RELEASES_WRITE_JWT
    def request = put(UPDATE_RELEASE, 1)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .header("Authorization", "Bearer $RELEASES_WRITE_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == BAD_REQUEST.value()

    where:
    body << [null, ""]
  }
}
