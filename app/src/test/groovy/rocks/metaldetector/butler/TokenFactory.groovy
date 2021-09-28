package rocks.metaldetector.butler

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import rocks.metaldetector.butler.config.security.UserRole

class TokenFactory {

  static final String TEST_TOKEN_SECRET = "secret"

  static String generateAdminTestToken() {
    long currentTimeInMillis = System.currentTimeMillis()
    Jwts.builder()
        .setSubject("metal-release-butler.test")
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(new Date(currentTimeInMillis))
        .claim("auth", UserRole.ROLE_ADMINISTRATOR.name + "," + UserRole.ROLE_USER.name)
        .setIssuer("mrb")
        .setExpiration(new Date(currentTimeInMillis + 30_000l))
        .signWith(SignatureAlgorithm.HS512, TEST_TOKEN_SECRET.bytes.encodeBase64().toString())
        .compact()
  }

  static String generateUserTestToken() {
    long currentTimeInMillis = System.currentTimeMillis()
    Jwts.builder()
        .setSubject("metal-release-butler.test")
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(new Date(currentTimeInMillis))
        .claim("auth", UserRole.ROLE_USER.name)
        .setIssuer("mrb")
        .setExpiration(new Date(currentTimeInMillis + 30_000l))
        .signWith(SignatureAlgorithm.HS512, TEST_TOKEN_SECRET.bytes.encodeBase64().toString())
        .compact()
  }
}
