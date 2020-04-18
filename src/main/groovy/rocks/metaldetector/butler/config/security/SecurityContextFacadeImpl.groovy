package rocks.metaldetector.butler.config.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextFacadeImpl implements SecurityContextFacade {

  @Override
  void setAuthentication(Authentication authentication) {
    SecurityContextHolder.getContext().setAuthentication(authentication)
  }
}
