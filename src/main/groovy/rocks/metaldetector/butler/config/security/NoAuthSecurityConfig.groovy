package rocks.metaldetector.butler.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import rocks.metaldetector.butler.config.constants.Endpoints

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS

@Configuration
@EnableWebSecurity
@Profile("authentication-less-mode")
class NoAuthSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().ignoringAntMatchers(Endpoints.AntPattern.REST_ENDPOINTS)
        .and().sessionManagement().sessionCreationPolicy(STATELESS)
    http.requiresChannel().requestMatchers(new XForwardedProtoMatcher()).requiresSecure()
    http.authorizeRequests().anyRequest().permitAll()
  }
}
