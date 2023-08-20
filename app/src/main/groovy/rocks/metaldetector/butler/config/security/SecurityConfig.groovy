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
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.IMPORT_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASES_UNPAGINATED
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASE_IMAGES
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.STATISTICS
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.UPDATE_RELEASE

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

  @Autowired
  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(STATELESS) }
        .exceptionHandling {}
        .authorizeHttpRequests { registry ->
          registry
              .requestMatchers(ACTUATOR_ENDPOINTS).permitAll()
              .requestMatchers(GET, RELEASE_IMAGES).permitAll()
              .requestMatchers(RELEASES).hasAuthority("SCOPE_releases-read")
              .requestMatchers(UPDATE_RELEASE).hasAuthority("SCOPE_releases-write")
              .requestMatchers(RELEASES_UNPAGINATED).hasAuthority("SCOPE_releases-read-all")
              .requestMatchers(IMPORT_JOB, COVER_JOB).hasAuthority("SCOPE_import")
              .requestMatchers(STATISTICS).hasAuthority("SCOPE_statistics")
              .anyRequest().denyAll()
        }
        .oauth2ResourceServer {
          it
              .authenticationEntryPoint(jwtAuthenticationEntryPoint)
              .jwt {}
        }
    return http.build()
  }
}
