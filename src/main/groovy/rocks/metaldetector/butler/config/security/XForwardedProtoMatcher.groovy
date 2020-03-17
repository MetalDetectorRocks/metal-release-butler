package rocks.metaldetector.butler.config.security

import org.springframework.security.web.util.matcher.RequestMatcher

import javax.servlet.http.HttpServletRequest

class XForwardedProtoMatcher implements RequestMatcher {

  @Override
  boolean matches(HttpServletRequest request) {
    request.getHeader("X-Forwarded-Proto") != null
  }
}
