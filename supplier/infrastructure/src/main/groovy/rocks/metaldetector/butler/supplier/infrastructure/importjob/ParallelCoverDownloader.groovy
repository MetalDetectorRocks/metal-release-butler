package rocks.metaldetector.butler.supplier.infrastructure.importjob

import groovy.util.logging.Slf4j
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository

import java.util.concurrent.Future
import java.util.function.Function

@Slf4j
@Service
class ParallelCoverDownloader {

  @Autowired
  ThreadPoolTaskExecutor threadPoolTaskExecutor

  @Autowired
  ReleaseRepository releaseRepository

  @Transactional
  List<ReleaseEntity> downloadAndSave(List<ReleaseEntity> releaseBatch, Function<ReleaseEntity, Runnable> coverDownloadTask) {
    List<Future> futures = []

    releaseBatch.each {
      releaseEntity -> futures << threadPoolTaskExecutor.submit(coverDownloadTask.apply(releaseEntity))
    }.collect()

    futures*.get()
    releaseRepository.saveAll(releaseBatch)
    return releaseBatch
  }
}
