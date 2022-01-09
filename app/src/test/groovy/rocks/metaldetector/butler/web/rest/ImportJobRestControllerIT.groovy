package rocks.metaldetector.butler.web.rest

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

import static org.mockito.Mockito.reset
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.IMPORT_JOB

@SpringBootTest
@AutoConfigureMockMvc
class ImportJobRestControllerIT extends Specification implements WithIntegrationTestConfig {

  private static Jwt USER_JWT = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("scope", "releases-read")
      .build()

  private static Jwt ADMIN_JWT = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("scope", "import")
      .build()

  @SpringBean
  ImportJobService importJobService = Mock(ImportJobService)

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  @Autowired
  MockMvc mockMvc

  void tearDown() {
    reset(importJobService)
  }

  def "Admin can GET on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> ADMIN_JWT
    def request = get(IMPORT_JOB).header("Authorization", "Bearer $ADMIN_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "User cannot GET on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> USER_JWT
    def request = get(IMPORT_JOB).header("Authorization", "Bearer $USER_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }

  def "Admin can POST on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> ADMIN_JWT
    def request = post(IMPORT_JOB).header("Authorization", "Bearer $ADMIN_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "User cannot POST on import endpoint"() {
    given:
    jwtDecoder.decode(*_) >> USER_JWT
    def request = post(IMPORT_JOB).header("Authorization", "Bearer $USER_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }

  def "Admin can POST on cover reload endpoint"() {
    given:
    jwtDecoder.decode(*_) >> ADMIN_JWT
    def request = post(COVER_JOB).header("Authorization", "Bearer " + ADMIN_JWT.tokenValue)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "User cannot POST on cover reload endpoint"() {
    given:
    jwtDecoder.decode(*_) >> USER_JWT
    def request = post(COVER_JOB).header("Authorization", "Bearer " + USER_JWT.tokenValue)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }
}
