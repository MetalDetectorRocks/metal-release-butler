package rocks.metaldetector.butler.testutil

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@TestPropertySource(locations = "classpath:application-integrationtest.yml")
@ActiveProfiles(["integrationtest", "default"])
interface WithIntegrationTestProfile {
}