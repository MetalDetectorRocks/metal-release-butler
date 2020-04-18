package rocks.metaldetector.butler.config.security.filter

import groovy.util.logging.Slf4j
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import rocks.metaldetector.butler.config.security.JwtsSupport
import rocks.metaldetector.butler.config.security.SecurityContextFacade

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Slf4j
@ConditionalOnProperty(
        name = "rocks.metaldetector.authentication.enabled",
        havingValue = "true",
        matchIfMissing = true
)
class JwtRequestFilterImpl extends OncePerRequestFilter implements JwtRequestFilter {

  static final HEADER_NAME = "Authorization"
  static final TOKEN_PREFIX = "Bearer "
  static final EMPTY_PASSWORD = ""

  @Autowired
  JwtsSupport jwtsSupport

  @Autowired
  SecurityContextFacade securityContextFacade

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String tokenHeader = request.getHeader(HEADER_NAME)

    if (tokenHeader != null && !tokenHeader.isEmpty() && tokenHeader.startsWith(TOKEN_PREFIX)) {
      def token = tokenHeader.substring(TOKEN_PREFIX.length())
      Claims claims

      // Verify token
      try {
        claims = jwtsSupport.getClaims(token)
      }
      catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
        log.warn("Unable to get claims", ex)
      }

      // Get authorities from claims
      if (claims) {
        def authorities = jwtsSupport.getAuthorities(claims)

        // Create principal und UsernamePasswordAuthenticationToken to set into SecurityContext for the current request
        def principal = new User(claims.getSubject(), EMPTY_PASSWORD, authorities)
        def usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principal, token, authorities)
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request))

        securityContextFacade.setAuthentication(usernamePasswordAuthenticationToken)
      }
    }
    else {
      log.warn("Token not present")
    }

    filterChain.doFilter(request, response)
  }
}
