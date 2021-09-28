package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.TokenFactory
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import rocks.metaldetector.butler.web.api.ReleaseUpdateRequest
import rocks.metaldetector.butler.web.api.ReleasesRequest
import rocks.metaldetector.butler.web.api.ReleasesRequestPaginated
import spock.lang.Specification

import static org.mockito.Mockito.reset
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.FAULTY
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES_UNPAGINATED
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.UPDATE_RELEASE

@SpringBootTest
@AutoConfigureMockMvc
class ReleasesRestControllerIT extends Specification implements WithIntegrationTestConfig {

  @SpringBean
  ReleaseService releaseService = Mock()

  @Autowired
  MockMvc mockMvc

  ObjectMapper objectMapper = new ObjectMapper()
  String testAdminToken = TokenFactory.generateAdminTestToken()
  String testUserToken = TokenFactory.generateUserTestToken()

  void tearDown() {
    reset(releaseService)
  }

  def "User can access releases endpoint"() {
    given:
    def requestBody = new ReleasesRequestPaginated(page: 1, size: 10, artists: [])
    def request = post(RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testUserToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "Admin can access releases endpoint"() {
    given:
    def requestBody = new ReleasesRequestPaginated(page: 1, size: 10, artists: [])
    def request = post(RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "Admin can access unpaginated releases endpoint"() {
    given:
    def requestBody = new ReleasesRequest(artists: [])
    def request = post(RELEASES_UNPAGINATED)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testAdminToken)
    releaseService.findAllUpcomingReleases(_) >> []

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "User cannot access unpaginated releases endpoint"() {
    given:
    def requestBody = new ReleasesRequest(artists: [])
    def request = post(RELEASES_UNPAGINATED)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testUserToken)
    releaseService.findAllUpcomingReleases(_) >> []

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.FORBIDDEN.value()
  }

  def "User cannot access release update endpoint"() {
    given:
    def requestBody = new ReleaseUpdateRequest(state: FAULTY)
    def request = put(UPDATE_RELEASE, 1)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testUserToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.FORBIDDEN.value()
  }

  def "Admin can access release update endpoint"() {
    given:
    def requestBody = new ReleaseUpdateRequest(state: FAULTY)
    def request = put(UPDATE_RELEASE, 1)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }
}
