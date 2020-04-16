package rocks.metaldetector.butler.config.security

import groovy.util.logging.Slf4j
import org.springframework.context.annotation.Profile
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.UNAUTHORIZED

@Component
@Slf4j
@Profile(["default", "preview", "prod"])
class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
    log.warn("Unauthorized request", authException.message)
    response.sendError(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase())
  }
}
