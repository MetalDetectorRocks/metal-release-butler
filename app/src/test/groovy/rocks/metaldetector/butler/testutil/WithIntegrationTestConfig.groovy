package rocks.metaldetector.butler.testutil

import org.springframework.test.context.TestPropertySource

@TestPropertySource(locations = "classpath:integrationtest.properties")
interface WithIntegrationTestConfig {
}