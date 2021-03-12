package rocks.metaldetector.butler.config.security.filter

import io.jsonwebtoken.impl.DefaultClaims
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.util.pattern.PathPatternParser
import rocks.metaldetector.butler.config.constants.Endpoints
import rocks.metaldetector.butler.config.security.JwtsSupport
import rocks.metaldetector.butler.config.security.SecurityContextFacade
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.FilterChain

class JwtRequestFilterImplTest extends Specification {

  JwtRequestFilterImpl underTest = new JwtRequestFilterImpl(
      jwtsSupport: Mock(JwtsSupport),
      pathPatternParser: Spy(PathPatternParser),
      securityContextFacade: Mock(SecurityContextFacade)
  )
  MockHttpServletRequest mockRequest = new MockHttpServletRequest()
  MockHttpServletResponse mockResponse = new MockHttpServletResponse()
  FilterChain filterChain = Mock(FilterChain)

  @Unroll
  "FilterChain is called for every request"() {
    given:
    mockRequest.servletPath = servletPath

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * filterChain.doFilter(mockRequest, mockResponse)

    where:
    servletPath << ["/actuator/health", Endpoints.RELEASES]
  }

  def "JwtsSupport is called to get claims when token is present"() {
    given:
    mockRequest.addHeader("Authorization", "Bearer token")

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.jwtsSupport.getClaims(_)
  }

  def "JwtsSupport is not called when token is empty"() {
    given:
    mockRequest.addHeader("Authorization", "")

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    0 * underTest.jwtsSupport.getClaims(_)
  }

  def "Token has to start with prefix 'Bearer '"() {
    given:
    mockRequest.addHeader("Authorization", "token")

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    0 * underTest.jwtsSupport.getClaims(_)
  }

  def "JwtsSupport is called with token prefix cut off"() {
    given:
    def token = "Bearer token"
    mockRequest.addHeader("Authorization", token)

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.jwtsSupport.getClaims(token.substring(7))
  }

  def "On verification success authorities are taken from token"() {
    given:
    def claims = new DefaultClaims()
    claims.setSubject("subject")
    mockRequest.addHeader("Authorization", "Bearer token")
    underTest.jwtsSupport.getClaims(_) >> claims

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.jwtsSupport.getAuthorities(claims) >> [new SimpleGrantedAuthority("user")]
  }

  def "On verification success an authentication object is set via SecurityContextFacade"() {
    given:
    def username = "john.doe"
    def authorities = [new SimpleGrantedAuthority("user")]
    def claims = new DefaultClaims()
    claims.setSubject(username)
    mockRequest.addHeader("Authorization", "Bearer token")
    underTest.jwtsSupport.getClaims(_) >> claims
    underTest.jwtsSupport.getAuthorities(claims) >> authorities

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.securityContextFacade.setAuthentication({ Authentication authentication ->
      assert authentication.name == username
      assert authentication.authorities == authorities
    })
  }

  def "PathPatternParser is called with actuator pattern"() {
    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.pathPatternParser.parse(Endpoints.AntPattern.ACTUATOR_ENDPOINTS)
  }

  def "If request path is any actuator path, token is not checked"() {
    given:
    mockRequest.servletPath = "/actuator/health"

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    0 * underTest.jwtsSupport(*_)
    0 * underTest.securityContextFacade(*_)
  }
}
