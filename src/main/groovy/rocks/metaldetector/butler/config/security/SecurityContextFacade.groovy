package rocks.metaldetector.butler.config.security

import org.springframework.security.core.context.SecurityContext

interface SecurityContextFacade {

  SecurityContext getContext()

  void setContext(SecurityContext securityContext)
}