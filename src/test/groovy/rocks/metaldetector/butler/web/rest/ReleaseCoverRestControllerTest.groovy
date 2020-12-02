package rocks.metaldetector.butler.web.rest

import org.springframework.core.io.ClassPathResource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rocks.metaldetector.butler.service.cover.ImageResourceFinder
import rocks.metaldetector.butler.testutil.WithExceptionResolver
import spock.lang.Specification

import java.nio.file.Paths

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASE_IMAGES

class ReleaseCoverRestControllerTest extends Specification implements WithExceptionResolver {

  ReleaseCoverRestController underTest = new ReleaseCoverRestController(imageResourceFinder: Mock(ImageResourceFinder))
  MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest, exceptionResolver()).build()

  def "should call image resource finder with image id"() {
    given:
    def coverId = "cover-id"
    def request = get("${RELEASE_IMAGES}/{id}", coverId)

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.imageResourceFinder.findImage(Paths.get("images", coverId))
  }

  def "should return 404 if no image was found"() {
    given:
    def request = get("${RELEASE_IMAGES}/{id}", "cover-id")
    underTest.imageResourceFinder.findImage(_) >> Optional.empty()

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == NOT_FOUND.value()
  }

  def "should return OK if image was found"() {
    given:
    def request = get("${RELEASE_IMAGES}/{id}", "cover-id")
    underTest.imageResourceFinder.findImage(_) >> Optional.of(new ClassPathResource("test.jpg"))

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }
}
