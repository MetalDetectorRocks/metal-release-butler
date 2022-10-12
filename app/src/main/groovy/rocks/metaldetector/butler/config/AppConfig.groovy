package rocks.metaldetector.butler.config

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.web.util.pattern.PathPatternParser

@Configuration
@EnableScheduling
@EnableAsync
class AppConfig {

  @Bean
  JwtParser jwtParser() {
    return Jwts.parserBuilder().build()
  }

  @Bean
  JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
    return new JwtGrantedAuthoritiesConverter()
  }

  @Bean
  PathPatternParser pathPatternParser() {
    return new PathPatternParser()
  }
}
