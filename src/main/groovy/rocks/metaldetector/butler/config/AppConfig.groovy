package rocks.metaldetector.butler.config

import groovy.xml.XmlSlurper
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.util.pattern.PathPatternParser

@Configuration
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
class AppConfig {

  @Autowired
  ConcurrencyConfig concurrencyConfig

  @Bean
  JwtParser jwtParser() {
    return Jwts.parser()
  }

  @Bean
  ThreadPoolTaskExecutor coverTransferThreadPool() {
    return new ThreadPoolTaskExecutor(corePoolSize: concurrencyConfig.releaseImportPoolSize)
  }

  @Bean
  XmlSlurper xmlSlurper() {
    return new XmlSlurper()
  }

  @Bean
  PathPatternParser pathPatternParser() {
    return new PathPatternParser()
  }
}
