package rocks.metaldetector.butler

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("preview")
class MetalReleaseButlerApplicationPreviewIntegrationTest extends Specification implements WithIntegrationTestConfig {

  def "Should load the application context with preview profile"() {
    expect:
    true
  }
}
