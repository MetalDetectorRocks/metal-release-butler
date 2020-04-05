package rocks.metaldetector.butler.config.security

import io.jsonwebtoken.impl.DefaultClaims
import org.junit.jupiter.api.BeforeEach
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import spock.lang.Specification

import javax.servlet.FilterChain

class JwtRequestFilterTest extends Specification {

  JwtRequestFilter underTest = new JwtRequestFilter(jwtsSupport: Mock(JwtsSupport),
                                                    securityContextFacade: Mock(SecurityContextFacade))
  MockHttpServletRequest mockRequest
  MockHttpServletResponse mockResponse
  FilterChain filterChain

  @BeforeEach
  def setup() {
    mockRequest = new MockHttpServletRequest()
    mockResponse = new MockHttpServletResponse()
    filterChain = Mock(FilterChain)
  }

  def "FilterChain is called"() {
    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * filterChain.doFilter(mockRequest, mockResponse)
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
    underTest.securityContextFacade.getContext() >> Mock(SecurityContext)

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.jwtsSupport.getAuthorities(claims) >> [new SimpleGrantedAuthority("user")]
  }

  def "On verification success SecurityContextFacade is called to set authorities"() {
    given:
    def claims = new DefaultClaims()
    claims.setSubject("subject")
    mockRequest.addHeader("Authorization", "Bearer token")
    underTest.jwtsSupport.getClaims(_) >> claims
    underTest.jwtsSupport.getAuthorities(claims) >> [new SimpleGrantedAuthority("user")]

    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.securityContextFacade.getContext() >> Mock(SecurityContext)
  }
}
