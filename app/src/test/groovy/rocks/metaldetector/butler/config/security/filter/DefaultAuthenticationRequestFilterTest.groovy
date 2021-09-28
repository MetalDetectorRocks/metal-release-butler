package rocks.metaldetector.butler.config.security.filter

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import rocks.metaldetector.butler.config.security.JwtsSupport
import rocks.metaldetector.butler.config.security.SecurityContextFacade
import spock.lang.Specification

import javax.servlet.FilterChain

import static rocks.metaldetector.butler.config.security.UserRole.ROLE_ADMINISTRATOR
import static rocks.metaldetector.butler.config.security.UserRole.ROLE_USER

class DefaultAuthenticationRequestFilterTest extends Specification {

  DefaultAuthenticationRequestFilter underTest = new DefaultAuthenticationRequestFilter(
          jwtsSupport: Mock(JwtsSupport),
          securityContextFacade: Mock(SecurityContextFacade)
  )

  MockHttpServletRequest mockRequest = new MockHttpServletRequest()
  MockHttpServletResponse mockResponse = new MockHttpServletResponse()
  FilterChain filterChain = Mock(FilterChain)

  def "By default, an administrator is authenticated"() {
    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * underTest.securityContextFacade.setAuthentication({ Authentication authentication ->
      assert authentication.name == "Default"
      assert authentication.authorities == [
              new SimpleGrantedAuthority(ROLE_ADMINISTRATOR.name),
              new SimpleGrantedAuthority(ROLE_USER.name)
      ]
    })
  }

  def "FilterChain is called"() {
    when:
    underTest.doFilterInternal(mockRequest, mockResponse, filterChain)

    then:
    1 * filterChain.doFilter(mockRequest, mockResponse)
  }
}
