package rocks.metaldetector.butler.service.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.MetalArchivesReleaseEntityConverter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient
import spock.lang.Specification
import spock.lang.Unroll

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES

class MetalArchivesReleaseImporterTest extends Specification {

  MetalArchivesReleaseImporter underTest = new MetalArchivesReleaseImporter(
      restClient: Mock(MetalArchivesRestClient),
      releaseEntityConverter: Mock(MetalArchivesReleaseEntityConverter),
      metalArchivesCoverService: Mock(CoverService),
      releaseRepository: Mock(ReleaseRepository),
      threadPool: Mock(ThreadPoolTaskExecutor)
  )

  def "rest client is called once on import"() {
    given:
    underTest.releaseRepository.saveAll(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.restClient.requestReleases() >> []
  }

  @Unroll
  "release converter is called for every response from rest template"() {
    given:
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

  def "a reissue task is created for every new release"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A"), ReleaseEntityFactory.createReleaseEntity("B")]
    underTest.restClient.requestReleases() >> [new String[0]]
    underTest.releaseEntityConverter.convert(*_) >> releaseEntities
    underTest.releaseRepository.saveAll(*_) >> releaseEntities

    when:
    underTest.importReleases()

    then:
    2 * underTest.threadPool.submit({ args -> args instanceof MetalArchivesReissueTask })
  }

  def "new releases are saved after reissue task"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A"), ReleaseEntityFactory.createReleaseEntity("B")]
    def newReleaseEntity = [ReleaseEntityFactory.createReleaseEntity("A")]
    underTest.restClient.requestReleases() >> [new String[0]]
    underTest.releaseEntityConverter.convert(*_) >> releaseEntities
    underTest.releaseRepository.saveAll(*_) >> newReleaseEntity

    when:
    underTest.importReleases()

    then:
    1 * underTest.threadPool.submit({ args -> args instanceof MetalArchivesReissueTask })

    then:
    underTest.releaseRepository.saveAll(newReleaseEntity)
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
