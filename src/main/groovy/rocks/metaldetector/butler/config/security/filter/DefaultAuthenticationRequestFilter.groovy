package rocks.metaldetector.butler.config.security.filter

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
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

import static rocks.metaldetector.butler.config.security.UserRole.ROLE_ADMINISTRATOR
import static rocks.metaldetector.butler.config.security.UserRole.ROLE_USER

@Component
@Slf4j
@ConditionalOnProperty(
        name = "rocks.metaldetector.authentication.enabled",
        havingValue = "false",
        matchIfMissing = false
)
class DefaultAuthenticationRequestFilter extends OncePerRequestFilter implements JwtRequestFilter {

  @Autowired
  JwtsSupport jwtsSupport

  @Autowired
  SecurityContextFacade securityContextFacade

  @Override
  void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    def authorities = [
            new SimpleGrantedAuthority(ROLE_ADMINISTRATOR.name),
            new SimpleGrantedAuthority(ROLE_USER.name)
    ]
    def principal = new User("Default", "", authorities)
    def usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principal, "Default", authorities)
    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request))

    securityContextFacade.setAuthentication(usernamePasswordAuthenticationToken)
    filterChain.doFilter(request, response)
  }
}
