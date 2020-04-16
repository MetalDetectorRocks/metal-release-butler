package rocks.metaldetector.butler.config.security

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import rocks.metaldetector.butler.config.constants.Endpoints

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile(["default", "preview", "prod"])
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint

  @Autowired
  JwtRequestFilter jwtRequestFilter

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().ignoringAntMatchers(Endpoints.AntPattern.REST_ENDPOINTS)
        .and().sessionManagement().sessionCreationPolicy(STATELESS)
        .and().requiresChannel().requestMatchers(new XForwardedProtoMatcher()).requiresSecure()
    http.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .and().authorizeRequests().anyRequest().authenticated()
        .and().addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter)
  }

  @Bean
  JwtParser jwtParser() {
    Jwts.parser()
  }
}
