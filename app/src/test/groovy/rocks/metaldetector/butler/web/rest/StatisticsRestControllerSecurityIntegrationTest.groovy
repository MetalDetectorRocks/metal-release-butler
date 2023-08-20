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
import rocks.metaldetector.butler.service.statistics.StatisticsService
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.STATISTICS

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class StatisticsRestControllerSecurityIntegrationTest extends Specification implements WithIntegrationTestConfig {

  private final Jwt OTHER_SCOPE_JWT = createTokenWithScope("other-scope")

  @Autowired
  MockMvc mockMvc

  @Autowired
  ObjectMapper objectMapper

  @SpringBean
  StatisticsService statisticService = Mock(StatisticsService)

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  def "Scope 'statistics' can access statistics endpoint"() {
    given:
    def token = createTokenWithScope("statistics")
    jwtDecoder.decode(*_) >> token
    def request = get(STATISTICS)
        .accept(APPLICATION_JSON)
        .header("Authorization", "Bearer $token.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Other scopes cannot access statistics endpoint"() {
    given:
    jwtDecoder.decode(*_) >> OTHER_SCOPE_JWT
    def request = get(STATISTICS)
        .accept(APPLICATION_JSON)
        .header("Authorization", "Bearer $OTHER_SCOPE_JWT.tokenValue")

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == FORBIDDEN.value()
  }
}
