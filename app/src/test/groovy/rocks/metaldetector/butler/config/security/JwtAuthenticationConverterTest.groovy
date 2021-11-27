package rocks.metaldetector.butler.config.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import spock.lang.Specification

class JwtAuthenticationConverterTest extends Specification {

  JwtAuthenticationConverter underTest = new JwtAuthenticationConverter(grantedAuthoritiesConverter: GroovyMock(JwtGrantedAuthoritiesConverter))

  def "grantedAuthoritiesConverter is called"() {
    given:
    def jwt = Mock(Jwt)

    when:
    underTest.convert(jwt)

    then:
    1 * underTest.grantedAuthoritiesConverter.convert(jwt)
  }

  def "scope 'user' is converted"() {
    given:
    underTest.grantedAuthoritiesConverter.convert(*_) >> [new SimpleGrantedAuthority("SCOPE_user")]

    when:
    def result = underTest.convert(Mock(Jwt))

    then:
    result.authorities.size() == 1

    and:
    result.authorities == [new SimpleGrantedAuthority("ROLE_USER")]
  }

  def "scope 'admin' is converted"() {
    given:
    underTest.grantedAuthoritiesConverter.convert(*_) >> [new SimpleGrantedAuthority("SCOPE_admin")]

    when:
    def result = underTest.convert(Mock(Jwt))

    then:
    result.authorities == [new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")]
  }

  def "other scopes are converted to empty list"() {
    given:
    underTest.grantedAuthoritiesConverter.convert(*_) >> [new SimpleGrantedAuthority("SCOPE_something")]

    when:
    def result = underTest.convert(Mock(Jwt))

    then:
    !result.authorities
  }
}
