package rocks.metaldetector.butler.supplier.metalarchives.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesReleaseVersionsWebCrawler
import spock.lang.Specification

import java.util.concurrent.locks.ReentrantReadWriteLock

import static rocks.metaldetector.butler.supplier.metalarchives.DtoFactory.ReleaseEntityFactory.createReleaseEntity

class ReissueHintEnhancerTest extends Specification {

  ReissueHintEnhancer underTest = new ReissueHintEnhancer(
      releaseRepository: Mock(ReleaseRepository),
      threadPoolTaskExecutor: Mock(ThreadPoolTaskExecutor),
      webCrawler: Mock(MetalArchivesReleaseVersionsWebCrawler),
      reentrantReadWriteLock: Mock(ReentrantReadWriteLock)
  )

  def "a reissue task is created for every new release"() {
    given:
    underTest.reentrantReadWriteLock.writeLock() >> Mock(ReentrantReadWriteLock.WriteLock)
    def release1 = createReleaseEntity("a")
    def release2 = createReleaseEntity("b")
    def releaseEntities = [release1, release2]

    when:
    underTest.enhance(releaseEntities)

    then:
    1 * underTest.threadPoolTaskExecutor.submit({ args ->
      args instanceof MetalArchivesReissueTask
          && args.releaseEntity == release1
          && args.webCrawler == underTest.webCrawler
    })

    and:
    1 * underTest.threadPoolTaskExecutor.submit({ args ->
      args instanceof MetalArchivesReissueTask
          && args.releaseEntity == release2
          && args.webCrawler == underTest.webCrawler
    })
  }

  def "should call release repository to save all new releases in a write lock"() {
    given:
    def writeLock = Mock(ReentrantReadWriteLock.WriteLock)
    def releaseEntities = [
        createReleaseEntity("a"),
        createReleaseEntity("b")
    ]

    when:
    underTest.enhance(releaseEntities)

    then:
    1 * underTest.reentrantReadWriteLock.writeLock() >> writeLock

    then:
    1 * writeLock.lock()

    then:
    1 * underTest.releaseRepository.saveAll(releaseEntities)

    then:
    1 * underTest.reentrantReadWriteLock.writeLock() >> writeLock

    then:
    1 * writeLock.unlock()
  }
}
