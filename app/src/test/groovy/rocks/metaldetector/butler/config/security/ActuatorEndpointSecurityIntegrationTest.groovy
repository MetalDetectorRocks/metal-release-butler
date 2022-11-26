package rocks.metaldetector.butler.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class ActuatorEndpointSecurityIntegrationTest extends Specification implements WithIntegrationTestConfig {

  @Autowired
  MockMvc mockMvc

  @Unroll
  "actuator endpoint #endpoint is not secured"() {
    given:
    def request = get(endpoint)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()

    where:
    endpoint << [
            "/actuator",
            "/actuator/health",
            "/actuator/info",
            "/actuator/metrics"
    ]
  }
}
