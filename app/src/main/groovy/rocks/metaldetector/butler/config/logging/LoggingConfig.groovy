package rocks.metaldetector.butler.config.logging

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class LoggingConfig {

  @Bean
  CommonsRequestLoggingFilter logFilter() {
    return new RestRequestLoggingFilter()
  }
}
