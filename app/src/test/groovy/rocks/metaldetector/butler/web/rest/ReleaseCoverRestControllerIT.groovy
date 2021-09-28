package rocks.metaldetector.butler.web.rest

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.TokenFactory
import rocks.metaldetector.butler.service.release.ImageResourceFinder
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

import static org.mockito.Mockito.reset
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASE_IMAGES

@SpringBootTest
@AutoConfigureMockMvc
class ReleaseCoverRestControllerIT extends Specification implements WithIntegrationTestConfig {

  @Autowired
  MockMvc mockMvc

  @Autowired
  ResourceLoader resourceLoader

  @SpringBean
  ImageResourceFinder imageResourceFinder = Mock()

  String testAdminToken = TokenFactory.generateAdminTestToken()
  String testUserToken = TokenFactory.generateUserTestToken()

  void setup() {
    Resource testImage = new ClassPathResource("test.jpg")
    imageResourceFinder.findImage(*_) >> Optional.of(testImage)
  }

  void tearDown() {
    reset(imageResourceFinder)
  }

  def "User can access release image endpoint"() {
    given:
    def request = get("$RELEASE_IMAGES?id={releaseCoverId}", "release-cover-id")
            .header("Authorization", "Bearer " + testUserToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }

  def "Admin can access release image endpoint"() {
    given:
    def request = get("$RELEASE_IMAGES?id={releaseCoverId}", "release-cover-id")
            .header("Authorization", "Bearer " + testAdminToken)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == HttpStatus.OK.value()
  }
}
