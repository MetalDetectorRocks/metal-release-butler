package rocks.metaldetector.butler.supplier.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.concurrent.locks.ReentrantReadWriteLock

@Configuration
class SynchronicityConfig {

  @Bean
  ReentrantReadWriteLock reentrantReadWriteLock() {
    new ReentrantReadWriteLock()
  }
}
