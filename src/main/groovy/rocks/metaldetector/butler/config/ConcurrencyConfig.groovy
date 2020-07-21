package rocks.metaldetector.butler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "concurrency")
class ConcurrencyConfig {

  int releaseImportPoolSize

}
