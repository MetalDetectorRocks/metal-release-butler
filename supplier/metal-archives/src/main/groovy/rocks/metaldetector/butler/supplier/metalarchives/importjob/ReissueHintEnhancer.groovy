package rocks.metaldetector.butler.supplier.metalarchives.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesReleaseVersionsWebCrawler

import java.util.concurrent.locks.ReentrantReadWriteLock

@Service
@Slf4j
class ReissueHintEnhancer {

  @Autowired
  ThreadPoolTaskExecutor threadPoolTaskExecutor

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  MetalArchivesReleaseVersionsWebCrawler webCrawler

  @Autowired
  ReentrantReadWriteLock reentrantReadWriteLock

  @Transactional
  void enhance(List<ReleaseEntity> newReleaseEntities) {
    log.info("Enhance reissue hint for ${newReleaseEntities.size()} releases...")
    def futures = newReleaseEntities.collect {
      threadPoolTaskExecutor.submit(createReissueTask(it))
    }

    futures*.get()

    reentrantReadWriteLock.writeLock().lock()
    try {
      releaseRepository.saveAll(newReleaseEntities)
    }
    finally {
      reentrantReadWriteLock.writeLock().unlock()
    }
  }

  private MetalArchivesReissueTask createReissueTask(ReleaseEntity releaseEntity) {
    return new MetalArchivesReissueTask(
        releaseEntity: releaseEntity,
        webCrawler: webCrawler
    )
  }
}
