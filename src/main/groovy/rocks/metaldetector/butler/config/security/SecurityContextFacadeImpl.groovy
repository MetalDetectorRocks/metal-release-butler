package rocks.metaldetector.butler.config.security

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextFacadeImpl implements SecurityContextFacade {

  @Override
  SecurityContext getContext() {
    SecurityContextHolder.getContext()
  }

  @Override
  void setContext(SecurityContext securityContext) {
    SecurityContextHolder.setContext(securityContext)
  }
}
