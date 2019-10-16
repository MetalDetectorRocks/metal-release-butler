package com.metalr2.butler.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableJpaAuditing
@EnableScheduling
class AppConfig {
}
