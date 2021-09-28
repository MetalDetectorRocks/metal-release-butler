package rocks.metaldetector.butler.config.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.impl.DefaultClaims
import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification

class JwtsSupportTest extends Specification {

  JwtsSupport underTest = new JwtsSupport(tokenSecret: "secret",
                                          jwtParser: Mock(JwtParser))

  def "SigningKey is set to validate token"() {
    given:
    underTest.jwtParser.parseClaimsJws(*_) >> Mock(Jws)

    when:
    underTest.getClaims("token")

    then:
    1 * underTest.jwtParser.setSigningKey(underTest.tokenSecret.bytes.encodeBase64().toString()) >> underTest.jwtParser
  }

  def "Token is passed to parser"() {
    given:
    def token = "token"
    underTest.jwtParser.setSigningKey(*_) >> underTest.jwtParser

    when:
    underTest.getClaims(token)

    then:
    1 * underTest.jwtParser.parseClaimsJws(token) >> Mock(Jws)
  }

  def "Claims are returned"() {
    given:
    def jws = Mock(Jws)
    def claims = Mock(Claims)
    underTest.jwtParser.setSigningKey(*_) >> underTest.jwtParser
    underTest.jwtParser.parseClaimsJws(*_) >> jws

    when:
    def result = underTest.getClaims("token")

    then:
    1 * jws.getBody() >> claims

    and:
    result instanceof Claims
  }

  def "Granted authorities are returned as collection"() {
    given:
    def claims = new DefaultClaims([auth: "User,Administrator"])

    when:
    def results = underTest.getAuthorities(claims)

    then:
    results.size() == 2

    and:
    results.each { it instanceof SimpleGrantedAuthority }

    and:
    results[0].authority == "User"
    results[1].authority == "Administrator"
  }
}
