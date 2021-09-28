package rocks.metaldetector.butler.persistence.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = ["rocks.metaldetector.butler.persistence"])
@EntityScan(basePackages = ["rocks.metaldetector.butler.persistence"])
@ComponentScan(basePackages = ["rocks.metaldetector.butler.persistence"])
class PersistenceConfig {
}
