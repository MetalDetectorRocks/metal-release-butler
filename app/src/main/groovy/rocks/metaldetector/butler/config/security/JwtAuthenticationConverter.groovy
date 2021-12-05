package rocks.metaldetector.butler.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Autowired
  JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter

  @Override
  AbstractAuthenticationToken convert(Jwt source) {
    def grantedAuthorities = grantedAuthoritiesConverter.convert(source)
        .collect {
          if (it.authority == "SCOPE_user") {
            return [new SimpleGrantedAuthority("ROLE_USER")]
          }
          if (it.authority == "SCOPE_admin") {
            return [new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")]
          }
          return []
        }.flatten() as List<GrantedAuthority>
    return new JwtAuthenticationToken(source, grantedAuthorities)
  }
}
