package rocks.metaldetector.butler.supplier.infrastructure.config

import groovy.xml.XmlSlurper
import org.ccil.cowan.tagsoup.Parser
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource

import java.time.Duration

import static java.time.temporal.ChronoUnit.MINUTES
import static java.time.temporal.ChronoUnit.SECONDS
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TEST

@Configuration
class InfrastructureConfig {

  @Bean
  XmlSlurper timeForMetalXmlSlurper() {
    return new XmlSlurper(new Parser())
  }

  @Bean
  XmlSlurper metalArchivesXmlSlurper() {
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

  @Bean
  ThreadPoolTaskExecutor releaseImportTaskExecutor() {
    return new ThreadPoolTaskExecutor(corePoolSize: [ReleaseSource.values() - TEST].size(),
                                      waitForTasksToCompleteOnShutdown: true,
                                      awaitTerminationSeconds: Duration.of(10, MINUTES).getSeconds())
  }
}
