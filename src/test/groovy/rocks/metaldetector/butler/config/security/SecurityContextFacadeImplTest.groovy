package rocks.metaldetector.butler.config.security

import org.springframework.security.core.context.SecurityContext
import spock.lang.Specification

class SecurityContextFacadeImplTest extends Specification {

  SecurityContextFacadeImpl underTest = new SecurityContextFacadeImpl()

  def "SecurityContext is returned"() {
    when:
    def result = underTest.getContext()

    then:
    result

    and:
    result instanceof SecurityContext
  }

  def "SecurityContext is set"() {
    given:
    def mockContext = Mock(SecurityContext)

    when:
    underTest.setContext(mockContext)

    then:
    underTest.getContext() == mockContext
  }
}
