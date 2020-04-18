package rocks.metaldetector.butler

import org.springframework.boot.test.context.SpringBootTest
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

@SpringBootTest
class MetalReleaseButlerApplicationTests extends Specification implements WithIntegrationTestConfig {

  def "Should load the application context" () {
    expect:
    true
  }
}
