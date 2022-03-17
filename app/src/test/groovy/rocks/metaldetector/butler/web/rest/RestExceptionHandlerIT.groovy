package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import rocks.metaldetector.butler.web.api.ReleasesRequestPaginated
import spock.lang.Specification

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES

@SpringBootTest
@AutoConfigureMockMvc
class RestExceptionHandlerIT extends Specification implements WithIntegrationTestConfig {

  private final Jwt READ_JWT = createTokenWithScope("releases-read")

  @Autowired
  MockMvc mockMvc

  @Autowired
  ObjectMapper objectMapper

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  @SpringBean
  ReleaseService releaseService = Mock(ReleaseService)

  def "invalid request method returns 405"() {
    given:
    jwtDecoder.decode(*_) >> READ_JWT
    def request = get(RELEASES)
        .contentType(APPLICATION_JSON)
        .header("Authorization", "Bearer $READ_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == METHOD_NOT_ALLOWED.value()
  }

  def "invalid media type returns 415"() {
    given:
    jwtDecoder.decode(*_) >> READ_JWT
    def requestBody = new ReleasesRequestPaginated(page: 1, size: 10, artists: [])
    def request = post(RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(APPLICATION_XHTML_XML)
        .header("Authorization", "Bearer $READ_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == UNSUPPORTED_MEDIA_TYPE.value()
  }

  def "invalid request returns 422"() {
    given:
    jwtDecoder.decode(*_) >> READ_JWT
    def requestBody = new ReleasesRequestPaginated(page: -1, size: 10, artists: [])
    def request = post(RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(APPLICATION_JSON)
        .header("Authorization", "Bearer $READ_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == UNPROCESSABLE_ENTITY.value()
  }

  def "other errors return 500"() {
    given:
    jwtDecoder.decode(*_) >> READ_JWT
    def requestBody = new ReleasesRequestPaginated(page: 1, size: 10, artists: [])
    def request = post(RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(APPLICATION_JSON)
        .header("Authorization", "Bearer $READ_JWT.tokenValue")
    releaseService.findAllUpcomingReleases(*_) >> { throw new RuntimeException("boom") }

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == INTERNAL_SERVER_ERROR.value()
  }
}
