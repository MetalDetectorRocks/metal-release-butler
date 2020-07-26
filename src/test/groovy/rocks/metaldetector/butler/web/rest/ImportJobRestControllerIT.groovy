package rocks.metaldetector.butler.web.rest

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.TokenFactory
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

import static org.mockito.Mockito.reset
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.config.constants.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.config.constants.Endpoints.IMPORT_JOB

@SpringBootTest
@AutoConfigureMockMvc
class ImportJobRestControllerIT extends Specification implements WithIntegrationTestConfig {

  @SpringBean
  ImportJobService importJobService = Mock()

  @Autowired
  MockMvc mockMvc

  String testAdminToken = TokenFactory.generateAdminTestToken()
  String testUserToken = TokenFactory.generateUserTestToken()

  void tearDown() {
    reset(importJobService)
  }

  def "Admin can GET on import endpoint"() {
    given:
    def request = get(IMPORT_JOB).header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "User cannot GET on import endpoint"() {
    given:
    def request = get(IMPORT_JOB).header("Authorization", "Bearer " + testUserToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.FORBIDDEN.value()
  }

  def "Admin can POST on import endpoint"() {
    given:
    def request = post(IMPORT_JOB).header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "User cannot POST on import endpoint"() {
    given:
    def request = post(IMPORT_JOB).header("Authorization", "Bearer " + testUserToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.FORBIDDEN.value()
  }

  def "Admin can POST on cover reload endpoint"() {
    given:
    def request = post(COVER_JOB).header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "User cannot POST on cover reload endpoint"() {
    given:
    def request = post(COVER_JOB).header("Authorization", "Bearer " + testUserToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.FORBIDDEN.value()
  }
}
