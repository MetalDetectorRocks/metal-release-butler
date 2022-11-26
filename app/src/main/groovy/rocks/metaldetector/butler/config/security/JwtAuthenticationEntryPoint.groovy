package rocks.metaldetector.butler.config.security

import groovy.util.logging.Slf4j
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

import static org.springframework.http.HttpStatus.UNAUTHORIZED

@Component
@Slf4j
class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
    log.warn("Unauthorized request", authException.message)
    response.sendError(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase())
  }
}
