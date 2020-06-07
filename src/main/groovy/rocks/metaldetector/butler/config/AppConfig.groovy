package rocks.metaldetector.butler.config

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
class AppConfig {

  @Bean
  JwtParser jwtParser() {
    Jwts.parser()
  }

  @Bean
  ThreadPoolTaskExecutor releaseEntityPersistenceThreadPool() {
    return new ThreadPoolTaskExecutor(corePoolSize: 5)
  }
}
