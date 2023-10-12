package rocks.metaldetector.butler.supplier.metalarchives.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ParallelCoverDownloader
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient
import rocks.metaldetector.butler.supplier.metalarchives.converter.MetalArchivesReleaseEntityConverter
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.locks.ReentrantReadWriteLock

import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.supplier.metalarchives.DtoFactory.ReleaseEntityFactory.createReleaseEntity

class MetalArchivesReleaseImporterTest extends Specification {

  MetalArchivesReleaseImporter underTest = new MetalArchivesReleaseImporter(
      restClient: Mock(MetalArchivesRestClient),
      metalArchivesCoverService: Mock(CoverService),
      releaseEntityConverter: Mock(MetalArchivesReleaseEntityConverter),
      releaseRepository: Mock(ReleaseRepository),
      threadPoolTaskExecutor: Mock(ThreadPoolTaskExecutor),
      reissueHintEnhancer: Mock(ReissueHintEnhancer),
      coverDownloader: Mock(ParallelCoverDownloader),
      reentrantReadWriteLock: Mock(ReentrantReadWriteLock)
  )

  def "rest client is called once on import"() {
    given:
    underTest.reentrantReadWriteLock.readLock() >> Mock(ReentrantReadWriteLock.ReadLock)
    underTest.reentrantReadWriteLock.writeLock() >> Mock(ReentrantReadWriteLock.WriteLock)
    underTest.releaseRepository.saveAll(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.restClient.requestReleases() >> []
  }

  @Unroll
  "release converter is called for every response from rest template"() {
    given:
    underTest.reentrantReadWriteLock.readLock() >> Mock(ReentrantReadWriteLock.ReadLock)
    underTest.reentrantReadWriteLock.writeLock() >> Mock(ReentrantReadWriteLock.WriteLock)
    underTest.restClient.requestReleases() >> releases
    underTest.releaseRepository.saveAll(*_) >> []

    when:
    underTest.importReleases()

    then:
    releases.size() * underTest.releaseEntityConverter.convert(_) >> []

    where:
    releases << [
        [],
        [new String[0], new String[0]]
    ]
  }

  def "should call release hint enhancer"() {
    given:
    underTest.reentrantReadWriteLock.readLock() >> Mock(ReentrantReadWriteLock.ReadLock)
    underTest.reentrantReadWriteLock.writeLock() >> Mock(ReentrantReadWriteLock.WriteLock)
    underTest.restClient.requestReleases() >> [new String[0]]
    def releaseEntities = [createReleaseEntity("A"), createReleaseEntity("B")]
    underTest.releaseEntityConverter.convert(*_) >> releaseEntities
    underTest.releaseRepository.saveAll(*_) >> releaseEntities

    when:
    underTest.importReleases()

    then:
    1 * underTest.reissueHintEnhancer.enhance(*_)
  }

  def "should return METAL_ARCHIVES as release source"() {
    expect:
    underTest.getReleaseSource() == METAL_ARCHIVES
  }

  def "should return specific cover service"() {
    expect:
    underTest.metalArchivesCoverService == underTest.getCoverService()
  }
}
