package rocks.metaldetector.butler

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class MetalReleaseButlerApplicationTests {

  @Test
  void contextLoads() {
  }

}
