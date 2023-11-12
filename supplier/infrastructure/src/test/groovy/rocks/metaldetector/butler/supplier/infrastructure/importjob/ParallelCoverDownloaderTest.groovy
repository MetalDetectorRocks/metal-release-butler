package rocks.metaldetector.butler.supplier.infrastructure.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import spock.lang.Specification

import static rocks.metaldetector.butler.supplier.infrastructure.DtoFactory.ReleaseEntityFactory.createReleaseEntity

class ParallelCoverDownloaderTest extends Specification {

  ParallelCoverDownloader underTest = new ParallelCoverDownloader(
      threadPoolTaskExecutor: Mock(ThreadPoolTaskExecutor)
  )

  def "should submit CoverDownloadTask for each release"() {
    given:
    def release1 = createReleaseEntity("a")
    def release2 = createReleaseEntity("b")
    def releaseEntities = [release1, release2]

    when:
    underTest.download(releaseEntities, release -> new CoverDownloadTask(releaseEntity: release))

    then:
    1 * underTest.threadPoolTaskExecutor.submit({
      args -> args instanceof CoverDownloadTask && args.releaseEntity == release1
    })

    and:
    1 * underTest.threadPoolTaskExecutor.submit({
      args -> args instanceof CoverDownloadTask && args.releaseEntity == release2
    })
  }
}
