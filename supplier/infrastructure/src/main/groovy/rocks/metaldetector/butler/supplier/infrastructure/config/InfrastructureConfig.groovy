package rocks.metaldetector.butler.supplier.infrastructure.config

import groovy.xml.XmlSlurper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class InfrastructureConfig {

  @Bean
  XmlSlurper xmlSlurper() {
    return new XmlSlurper()
  }

  @Bean
  ThreadPoolTaskExecutor threadPoolTaskExecutor(@Value('${concurrency.release-import-pool-size}') int releaseImportPoolSize) {
    return new ThreadPoolTaskExecutor(corePoolSize: releaseImportPoolSize,
                                      waitForTasksToCompleteOnShutdown: true,
                                      awaitTerminationSeconds: 60)
  }
}
