package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import rocks.metaldetector.butler.web.api.ReleaseUpdateRequest
import rocks.metaldetector.butler.web.api.ReleasesRequest
import rocks.metaldetector.butler.web.api.ReleasesRequestPaginated
import spock.lang.Specification

import static org.mockito.Mockito.reset
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.FAULTY
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES_UNPAGINATED
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.UPDATE_RELEASE

@SpringBootTest
@AutoConfigureMockMvc
class ReleasesRestControllerIT extends Specification implements WithIntegrationTestConfig {

  private static Jwt USER_JWT = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("scope", "user")
      .build()

  private static Jwt ADMIN_JWT = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("scope", "admin")
      .build()

  @SpringBean
  ReleaseService releaseService = Mock(ReleaseService)

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  @Autowired
  MockMvc mockMvc

  ObjectMapper objectMapper = new ObjectMapper()

  void tearDown() {
    reset(releaseService)
  }

  def "User can access releases endpoint"() {
    given:
    jwtDecoder.decode(*_) >> USER_JWT
    def requestBody = new ReleasesRequestPaginated(page: 1, size: 10, artists: [])
    def request = post(RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + USER_JWT.getTokenValue())

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Admin can access releases endpoint"() {
    given:
    jwtDecoder.decode(*_) >> ADMIN_JWT
    def requestBody = new ReleasesRequestPaginated(page: 1, size: 10, artists: [])
    def request = post(RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + ADMIN_JWT.getTokenValue())

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Admin can access unpaginated releases endpoint"() {
    given:
    jwtDecoder.decode(*_) >> ADMIN_JWT
    def requestBody = new ReleasesRequest(artists: [])
    def request = post(RELEASES_UNPAGINATED)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + ADMIN_JWT.getTokenValue())
    releaseService.findAllUpcomingReleases(*_) >> []

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "User cannot access unpaginated releases endpoint"() {
    given:
    jwtDecoder.decode(*_) >> USER_JWT
    def requestBody = new ReleasesRequest(artists: [])
    def request = post(RELEASES_UNPAGINATED)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + USER_JWT.getTokenValue())
    releaseService.findAllUpcomingReleases(*_) >> []

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }

  def "User cannot access release update endpoint"() {
    given:
    jwtDecoder.decode(*_) >> USER_JWT
    def requestBody = new ReleaseUpdateRequest(state: FAULTY)
    def request = put(UPDATE_RELEASE, 1)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + USER_JWT.getTokenValue())

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }

  def "Admin can access release update endpoint"() {
    given:
    jwtDecoder.decode(*_) >> ADMIN_JWT
    def requestBody = new ReleaseUpdateRequest(state: FAULTY)
    def request = put(UPDATE_RELEASE, 1)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + ADMIN_JWT.getTokenValue())

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }
}
