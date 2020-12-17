package rocks.metaldetector.butler.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import rocks.metaldetector.butler.config.security.filter.JwtRequestFilter

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import static rocks.metaldetector.butler.config.constants.Endpoints.AntPattern.ACTUATOR_ENDPOINTS
import static rocks.metaldetector.butler.config.constants.Endpoints.AntPattern.REST_ENDPOINTS

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint

  @Autowired
  JwtRequestFilter jwtRequestFilter

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().ignoringAntMatchers(REST_ENDPOINTS)
        .and().sessionManagement().sessionCreationPolicy(STATELESS)
        .and().requiresChannel().requestMatchers(new XForwardedProtoMatcher()).requiresSecure()
    http.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .and().authorizeRequests()
            .antMatchers(ACTUATOR_ENDPOINTS).permitAll()
            .anyRequest().authenticated()
        .and().addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter)
  }
}
