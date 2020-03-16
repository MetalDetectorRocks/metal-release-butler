package rocks.metaldetector.butler

import org.springframework.boot.test.context.SpringBootTest
import rocks.metaldetector.butler.testutil.WithIntegrationTestProfile
import spock.lang.Specification

@SpringBootTest
class MetalReleaseButlerApplicationTests extends Specification implements WithIntegrationTestProfile {

  def "Should load the application context" () {
    expect:
    true
  }
}
