package rocks.metaldetector.butler.testutil

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.context.TestPropertySource

@TestPropertySource(locations = "classpath:integrationtest.properties")
interface WithIntegrationTestConfig {

  default Jwt createTokenWithScope(String scope) {
    return Jwt.withTokenValue("token")
        .header("alg", "none")
        .claim("scope", scope)
        .build()
  }
}