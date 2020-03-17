package rocks.metaldetector.butler.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.POST
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS

@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.sessionManagement().sessionCreationPolicy(STATELESS)
    http.requiresChannel().requestMatchers(new XForwardedProtoMatcher()).requiresSecure()
    http.authorizeRequests().antMatchers(GET, "/**").permitAll() // temporary until implementation of https://trello.com/c/x1MpE505
  }

  @Override
  void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers(POST, "/**") // temporary until implementation of https://trello.com/c/x1MpE505
  }
}
