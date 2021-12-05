package rocks.metaldetector.butler.web.rest

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.service.release.ImageResourceFinder
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

import static org.mockito.Mockito.reset
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASE_IMAGES

@SpringBootTest
@AutoConfigureMockMvc
class ReleaseCoverRestControllerIT extends Specification implements WithIntegrationTestConfig {

  private static Jwt USER_JWT = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("scope", "user")
      .build()

  private static Jwt ADMIN_JWT = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("scope", "admin")
      .build()

  @Autowired
  MockMvc mockMvc

  @Autowired
  ResourceLoader resourceLoader

  @SpringBean
  ImageResourceFinder imageResourceFinder = Mock(ImageResourceFinder)

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  void setup() {
    Resource testImage = new ClassPathResource("test.jpg")
    imageResourceFinder.findImage(*_) >> Optional.of(testImage)
  }

  void tearDown() {
    reset(imageResourceFinder)
  }

  def "User can access release image endpoint"() {
    given:
    jwtDecoder.decode(*_) >> USER_JWT
    def request = get("$RELEASE_IMAGES?id={releaseCoverId}", "release-cover-id")
            .header("Authorization", "Bearer " + USER_JWT.getTokenValue())

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Admin can access release image endpoint"() {
    given:
    jwtDecoder.decode(*_) >> ADMIN_JWT
    def request = get("$RELEASE_IMAGES?id={releaseCoverId}", "release-cover-id")
            .header("Authorization", "Bearer " + ADMIN_JWT.getTokenValue())

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }
}
