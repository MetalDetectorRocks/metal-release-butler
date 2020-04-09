package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.TokenFactory
import rocks.metaldetector.butler.config.constants.Endpoints
import rocks.metaldetector.butler.service.ReleaseService
import rocks.metaldetector.butler.testutil.WithIntegrationTestProfile
import rocks.metaldetector.butler.web.dto.ReleasesRequest
import rocks.metaldetector.butler.web.dto.ReleasesRequestPaginated
import spock.lang.Specification

import static org.mockito.Mockito.reset
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@AutoConfigureMockMvc
class ReleasesRestControllerIT extends Specification implements WithIntegrationTestProfile {

  @SpringBean
  ReleaseService releaseService = Mock()

  @Autowired
  MockMvc mockMvc

  ObjectMapper objectMapper
  String testAdminToken = TokenFactory.generateAdminTestToken()
  String testUserToken = TokenFactory.generateUserTestToken()

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapper()
  }

  @AfterEach
  void tearDown() {
    reset(releaseService)
  }

  /**
   * For some reason this test is needed. Otherwise gradle fails on the first
   * test after this one (no matter which one is put first). Running only the
   * test (or the whole class) via gradle works.
   * See: https://trello.com/c/DT2oQHal
   */
  def "Why am I here?"() {
    expect:
    true
  }

  def "User can access releases endpoint"() {
    given:
    def requestBody = new ReleasesRequestPaginated(page: 1, size: 10, artists: [])
    def request = post(Endpoints.RELEASES)
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
    def request = post(Endpoints.RELEASES)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "Admin can access import endpoint"() {
    given:
    def request = get(Endpoints.RELEASES).param("action", "import").header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "Admin can access unpaginated releases endpoint"() {
    given:
    def requestBody = new ReleasesRequest(artists: [])
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testAdminToken)
    releaseService.findAllUpcomingReleases(_) >> []

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "User cannot access import endpoint"() {
    given:
    def request = get(Endpoints.RELEASES).param("action", "import").header("Authorization", "Bearer " + testUserToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.FORBIDDEN.value()
  }

  def "User cannot access unpaginated releases endpoint"() {
    given:
    def requestBody = new ReleasesRequest(artists: [])
    def request = post(Endpoints.RELEASES + Endpoints.UNPAGINATED)
        .content(objectMapper.writeValueAsString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + testUserToken)
    releaseService.findAllUpcomingReleases(_) >> []

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.FORBIDDEN.value()
  }
}
