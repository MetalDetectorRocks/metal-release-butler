package rocks.metaldetector.butler

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class MetalReleaseButlerApplicationTests extends Specification {

  def "Should load the application context" () {
    expect:
    true
  }
}
