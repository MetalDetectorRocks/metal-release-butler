package rocks.metaldetector.butler.service.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.service.cover.NoOpCoverService
import spock.lang.Specification

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.TEST
import static rocks.metaldetector.butler.model.release.ReleaseSource.TIME_FOR_METAL

class AbstractReleaseImporterTest extends Specification {

  AbstractReleaseImporter underTest = new TestReleaseImporter(releaseRepository: Mock(ReleaseRepository),
                                                              coverTransferThreadPool: Mock(ThreadPoolTaskExecutor))

  def "finalizeImport: Duplicates are filtered out before the database query checks whether the release already exists"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("A"),
    ]

    when:
    underTest.finalizeImport(releaseEntities)

    then:
    1 * underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_)
  }

  def "finalizeImport: ReleaseEntity and CoverService is passed to each created CoverTransferTask"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("b")
    ]

    when:
    underTest.finalizeImport(releaseEntities)

    then:
    1 * underTest.coverTransferThreadPool.submit({ args ->
      args.releaseEntity == releaseEntities[0] &&
      args.coverService == underTest.getCoverService()
    })

    and:
    1 * underTest.coverTransferThreadPool.submit({ args ->
      args.releaseEntity == releaseEntities[1] &&
      args.coverService == underTest.getCoverService()
    })
  }

  def "finalizeImport: existing releases are not submitted to persistence thread pool"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("a")]

    when:
    underTest.finalizeImport(releaseEntities)

    then:
    1 * underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >> true

    and:
    0 * underTest.coverTransferThreadPool.submit(_)
  }

  def "finalizeImport: should call release repository to save all new releases"() {
    given:
    underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >>> [false, false]
    def releaseEntities = [
            ReleaseEntityFactory.createReleaseEntity("a"),
            ReleaseEntityFactory.createReleaseEntity("b")
    ]

    when:
    underTest.finalizeImport(releaseEntities)

    then:
    1 * underTest.releaseRepository.saveAll(releaseEntities)
  }

  def "finalizeImport: should return ImportResult with correct values for 'totalCountRequested' and 'totalCountImported'"() {
    given:
    underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >>> [true, false]
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("b")
    ]

    when:
    def importResult = underTest.finalizeImport(releaseEntities)

    then:
    importResult == new ImportResult(totalCountRequested: 2, totalCountImported: 1)
  }

  def "finalizeImport: should not modify original list when calling unique()"() {
    given:
    underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >>> [false, false]
    def releaseEntities = [
            ReleaseEntityFactory.createReleaseEntity("a"),
            ReleaseEntityFactory.createReleaseEntity("a"),
            ReleaseEntityFactory.createReleaseEntity("b")
    ]

    when:
    def importResult = underTest.finalizeImport(releaseEntities)

    then:
    importResult == new ImportResult(totalCountRequested: 3, totalCountImported: 2)
  }

  def "retryCoverDownload: should call releaseRepository on retryCoverDownload"() {
    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.releaseRepository.findAll()
  }

  def "retryCoverDownload: all releases of current release source without cover url are added to thread pool"() {
    given:
    def release1 = new ReleaseEntity(artist: "A", source: TEST)
    def release2 = new ReleaseEntity(artist: "B", source: METAL_ARCHIVES)
    def release3 = new ReleaseEntity(artist: "C", source: TIME_FOR_METAL)
    def release4 = new ReleaseEntity(artist: "D", source: TEST, coverUrl: "i'm an url")
    underTest.releaseRepository.findAll() >> [release1, release2, release3, release4]

    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.coverTransferThreadPool.submit({ args ->
      args.releaseEntity == release1
      args.coverService == underTest.getCoverService()
    })

    then:
    0 * underTest.coverTransferThreadPool.submit(*_)
  }

  def "retryCoverDownload: should call release repository to update releases"() {
    given:
    def release1 = new ReleaseEntity(artist: "A", source: TEST)
    def release2 = new ReleaseEntity(artist: "B", source: TEST)
    def releases = [release1, release2]
    underTest.releaseRepository.findAll() >> releases

    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.releaseRepository.saveAll(releases)
  }

  class TestReleaseImporter extends AbstractReleaseImporter {

    CoverService coverService = new NoOpCoverService()

    @Override
    ImportResult importReleases() {
      return null
    }

    @Override
    ReleaseSource getReleaseSource() {
      return TEST
    }

    @Override
    CoverService getCoverService() {
      return coverService
    }
  }
}
