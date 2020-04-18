package rocks.metaldetector.butler.config.security

import org.springframework.security.core.Authentication

interface SecurityContextFacade {

  void setAuthentication(Authentication authentication)
}