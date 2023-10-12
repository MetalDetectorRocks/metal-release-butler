package rocks.metaldetector.butler.supplier.infrastructure.importjob

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity

import java.util.concurrent.Future
import java.util.function.Function

@Service
class ParallelCoverDownloader {

  @Autowired
  ThreadPoolTaskExecutor threadPoolTaskExecutor

  void download(List<ReleaseEntity> releaseBatch, Function<ReleaseEntity, Runnable> coverDownloadTask) {
    List<Future> futures = []
    releaseBatch.each {
      releaseEntity -> futures << threadPoolTaskExecutor.submit(coverDownloadTask.apply(releaseEntity))
    }
    futures*.get()
  }
}
