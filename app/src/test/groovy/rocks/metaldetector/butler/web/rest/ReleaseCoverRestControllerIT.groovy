package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import rocks.metaldetector.butler.service.release.ImageResourceFinder
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
class ReleaseCoverRestControllerIT extends Specification implements WithIntegrationTestConfig {

  private final Jwt RELEASES_READ_JWT = createTokenWithScope("releases-read")

  @Autowired
  MockMvc mockMvc

  @Autowired
  ObjectMapper objectMapper

  @SpringBean
  JwtDecoder jwtDecoder = Mock(JwtDecoder)

  @SpringBean
  ImageResourceFinder imageResourceFinder = Mock(ImageResourceFinder)

//  def "should return NOT FOUND for wrong path"() {
//    given:
//    jwtDecoder.decode(*_) >> RELEASES_READ_JWT
//    def request = get("${RELEASE_IMAGES}/?id=test-id")
//        .accept(IMAGE_JPEG_VALUE, IMAGE_GIF_VALUE, IMAGE_PNG_VALUE)
//        .header("Authorization", "Bearer $RELEASES_READ_JWT.tokenValue")
//    imageResourceFinder.findImage(*_) >> { throw new ResourceNotFoundException("not found") }
//
//    when:
//    def result = mockMvc.perform(request).andReturn()
//
//    then:
//    result.response.status == NOT_FOUND.value()
//  }
}
