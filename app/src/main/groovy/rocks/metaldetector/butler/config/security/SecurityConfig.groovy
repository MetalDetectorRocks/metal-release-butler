package rocks.metaldetector.butler.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.AntPattern.ACTUATOR_ENDPOINTS
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.AntPattern.REST_ENDPOINTS

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint

  @Autowired
  JwtAuthenticationConverter jwtAuthenticationConverter

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().ignoringAntMatchers(REST_ENDPOINTS)
        .and().sessionManagement().sessionCreationPolicy(STATELESS)
    http.exceptionHandling()
        .and().authorizeRequests()
        .antMatchers(ACTUATOR_ENDPOINTS).permitAll()
        .anyRequest().authenticated()
    http.oauth2ResourceServer().authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .jwt()
        .jwtAuthenticationConverter(jwtAuthenticationConverter)
  }
}
