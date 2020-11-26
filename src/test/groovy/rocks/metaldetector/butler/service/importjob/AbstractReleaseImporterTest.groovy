package rocks.metaldetector.butler.service.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.cover.CoverService
import spock.lang.Specification

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE
import static rocks.metaldetector.butler.model.release.ReleaseSource.TEST

class AbstractReleaseImporterTest extends Specification {

  AbstractReleaseImporter underTest = new TestReleaseImporter(releaseRepository: Mock(ReleaseRepository),
                                                              releaseEntityPersistenceThreadPool: Mock(ThreadPoolTaskExecutor),
                                                              coverService: Mock(CoverService))

  def "Duplicates are filtered out before the database query checks whether the release already exists"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("a")
    ]

    when:
    underTest.persistReleaseEntities(releaseEntities)

    then:
    1 * underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_)
  }

  def "ReleaseEntity, CoverService and ReleaseRepository is passed to each created PersistReleaseEntityTask"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("b")
    ]

    when:
    underTest.persistReleaseEntities(releaseEntities)

    then:
    1 * underTest.releaseEntityPersistenceThreadPool.submit({ args ->
      args.releaseEntity == releaseEntities[0] &&
      args.coverService == underTest.coverService &&
      args.releaseRepository == underTest.releaseRepository
    })

    and:
    1 * underTest.releaseEntityPersistenceThreadPool.submit({ args ->
      args.releaseEntity == releaseEntities[1] &&
      args.coverService == underTest.coverService &&
      args.releaseRepository == underTest.releaseRepository
    })
  }

  def "existing releases are not submitted to persistence thread pool"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("a")]

    when:
    underTest.persistReleaseEntities(releaseEntities)

    then:
    1 * underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >> true

    and:
    0 * underTest.releaseEntityPersistenceThreadPool.submit(_)
  }

  def "should return ImportResult with correct values for 'totalCountRequested' and 'totalCountImported'"() {
    given:
    underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >>> [true, false]
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("b")
    ]

    when:
    def importResult = underTest.persistReleaseEntities(releaseEntities)

    then:
    importResult == new ImportResult(totalCountRequested: 2, totalCountImported: 1)
  }

  def "should call releaseRepository on retryCoverDownload"() {
    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.releaseRepository.findAll()
  }

  def "all releases of current release source without cover url are added to thread pool"() {
    given:
    def release1 = new ReleaseEntity(artist: "A", source: TEST)
    def release2 = new ReleaseEntity(artist: "B", source: METAL_ARCHIVES)
    def release3 = new ReleaseEntity(artist: "C", source: METAL_HAMMER_DE)
    def release4 = new ReleaseEntity(artist: "D", source: TEST, coverUrl: "i'm an url")
    underTest.releaseRepository.findAll() >> [release1, release2, release3, release4]

    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.releaseEntityPersistenceThreadPool.submit({ args ->
      args.releaseEntity == release1 &&
      args.coverService == underTest.coverService &&
      args.releaseRepository == underTest.releaseRepository
    })

    then:
    0 * underTest.releaseEntityPersistenceThreadPool.submit(*_)
  }

  def "nothing is called if cover service is null"() {
    given:
    underTest.coverService = null

    when:
    underTest.retryCoverDownload()

    then:
    0 * underTest.releaseRepository.findAll()
  }

  class TestReleaseImporter extends AbstractReleaseImporter {

    @Override
    ImportResult importReleases() {
      return null
    }

    @Override
    ReleaseSource getReleaseSource() {
      return TEST
    }
  }
}