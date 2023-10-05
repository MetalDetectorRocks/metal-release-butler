package rocks.metaldetector.butler.web.rest

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.FETCH_IMPORT_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.IMPORT_JOB

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class ImportJobRestControllerIntegrationTest extends Specification implements WithIntegrationTestConfig {

  private final Jwt OTHER_SCOPE_JWT = createTokenWithScope("other-scope")
  private final Jwt IMPORT_JWT = createTokenWithScope("import")

  @SpringBean
  ImportJobService importJobService = Mock(ImportJobService)

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  @Autowired
  MockMvc mockMvc

  def "Scope 'import' can GET on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> IMPORT_JWT
    def request = get(IMPORT_JOB).header("Authorization", "Bearer $IMPORT_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Other scopes cannot GET on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> OTHER_SCOPE_JWT
    def request = get(IMPORT_JOB).header("Authorization", "Bearer $OTHER_SCOPE_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }

  def "Scope 'import' can GET on single import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> IMPORT_JWT
    def request = get(FETCH_IMPORT_JOB.replace("{jobId}", "123"))
        .header("Authorization", "Bearer $IMPORT_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Other scopes cannot GET on single import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> OTHER_SCOPE_JWT
    def request = get(FETCH_IMPORT_JOB.replace("{jobId}", "123"))
        .header("Authorization", "Bearer $OTHER_SCOPE_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }

  def "Scope 'import' can POST on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> IMPORT_JWT
    def request = post(IMPORT_JOB).header("Authorization", "Bearer $IMPORT_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Other scopes cannot POST on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> OTHER_SCOPE_JWT
    def request = post(IMPORT_JOB).header("Authorization", "Bearer $OTHER_SCOPE_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }

  def "Scope 'import' can POST on cover reload endpoint"() {
    given:
    jwtDecoder.decode(*_) >> IMPORT_JWT
    def request = post(COVER_JOB).header("Authorization", "Bearer " + IMPORT_JWT.tokenValue)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Other scopes cannot POST on cover reload endpoint"() {
    given:
    jwtDecoder.decode(*_) >> OTHER_SCOPE_JWT
    def request = post(COVER_JOB).header("Authorization", "Bearer " + OTHER_SCOPE_JWT.tokenValue)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }
}
