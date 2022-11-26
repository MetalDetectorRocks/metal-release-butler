package rocks.metaldetector.butler.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

import static org.springframework.http.HttpMethod.GET
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.AntPattern.ACTUATOR_ENDPOINTS
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.AntPattern.REST_ENDPOINTS
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASE_IMAGES

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

  @Autowired
  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf().ignoringRequestMatchers(REST_ENDPOINTS)
        .and().sessionManagement().sessionCreationPolicy(STATELESS)
    http.exceptionHandling()
        .and().authorizeHttpRequests()
        .requestMatchers(ACTUATOR_ENDPOINTS).permitAll()
        .requestMatchers(GET, RELEASE_IMAGES).permitAll()
        .anyRequest().authenticated()
    http.oauth2ResourceServer().authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .jwt()
    return http.build()
  }
}
