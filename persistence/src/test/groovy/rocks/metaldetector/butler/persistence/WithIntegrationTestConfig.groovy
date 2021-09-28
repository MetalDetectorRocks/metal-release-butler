package rocks.metaldetector.butler.persistence

import org.springframework.test.context.TestPropertySource

@TestPropertySource(locations = "classpath:integrationtest.properties")
interface WithIntegrationTestConfig {
}
