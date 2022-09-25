package rocks.metaldetector.butler.supplier.infrastructure.config

import groovy.xml.XmlSlurper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

import java.time.Duration

import static java.time.temporal.ChronoUnit.SECONDS

@Configuration
class InfrastructureConfig {

  @Bean
  XmlSlurper xmlSlurper() {
    return new XmlSlurper()
  }

  @Bean
  ThreadPoolTaskExecutor threadPoolTaskExecutor(@Value('${concurrency.release-import-pool-size}') int releaseImportPoolSize,
                                                @Value('${spring.task.execution.shutdown.await-termination}') boolean awaitTermination,
                                                @Value('${spring.task.execution.shutdown.await-termination-period}') Duration awaitTerminationPeriod) {
    return new ThreadPoolTaskExecutor(corePoolSize: releaseImportPoolSize,
                                      waitForTasksToCompleteOnShutdown: awaitTermination,
                                      awaitTerminationSeconds: awaitTerminationPeriod.get(SECONDS))
  }
}
