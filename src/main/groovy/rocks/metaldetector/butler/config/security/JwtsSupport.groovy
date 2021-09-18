package rocks.metaldetector.butler.config.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
@PropertySource(value = "classpath:application.yml")
class JwtsSupport {

  static final String AUTHORITIES_KEY = "auth"

  @Value('${security.token-secret}')
  String tokenSecret

  @Autowired
  JwtParser jwtParser

  Claims getClaims(String token) {
    jwtParser.setSigningKey(tokenSecret.bytes.encodeBase64().toString())
             .parseClaimsJws(token)
             .getBody()
  }

  Collection<? extends GrantedAuthority> getAuthorities(Claims claims) {
    claims.get(AUTHORITIES_KEY).toString().split(",").collect { new SimpleGrantedAuthority(it) }
  }
}
