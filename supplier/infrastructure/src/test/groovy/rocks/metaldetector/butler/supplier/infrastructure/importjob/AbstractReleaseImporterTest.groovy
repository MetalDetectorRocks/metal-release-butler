package rocks.metaldetector.butler.supplier.infrastructure.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService
import rocks.metaldetector.butler.supplier.infrastructure.cover.NoOpCoverService
import spock.lang.Specification

import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TEST
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TIME_FOR_METAL
import static rocks.metaldetector.butler.supplier.infrastructure.DtoFactory.ReleaseEntityFactory
import static rocks.metaldetector.butler.supplier.infrastructure.importjob.AbstractReleaseImporter.BATCH_SIZE

class AbstractReleaseImporterTest extends Specification {

  AbstractReleaseImporter underTest = new TestReleaseImporter(
      releaseRepository: Mock(ReleaseRepository),
      threadPoolTaskExecutor: Mock(ThreadPoolTaskExecutor),
      coverDownloader: Mock(ParallelCoverDownloader)
  )

  def "saveNewReleasesWithCover: Duplicates are filtered out before the database query checks whether the release already exists"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("A"),
    ]

    when:
    underTest.saveNewReleasesWithCover(releaseEntities)

    then:
    1 * underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_)
  }

  def "saveNewReleasesWithCover: existing releases are not submitted to persistence thread pool"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("a")]

    when:
    underTest.saveNewReleasesWithCover(releaseEntities)

    then:
    1 * underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >> true

    and:
    0 * underTest.threadPoolTaskExecutor.submit(_)
  }

  def "saveNewReleasesWithCover: should not modify original list when calling unique()"() {
    given:
    underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >>> [false, false]
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("a"),
        ReleaseEntityFactory.createReleaseEntity("b")
    ]

    when:
    underTest.saveNewReleasesWithCover(releaseEntities)

    then:
    1 * underTest.coverDownloader.downloadAndSave({ args ->
      args.size() < releaseEntities.size()
    }, _)
  }

  def "saveNewReleasesWithCover: should call cover downloader per each releases batch"() {
    given:
    def releaseEntities = []
    1.upto(BATCH_SIZE + 1, { num -> releaseEntities += ReleaseEntityFactory.createReleaseEntity("artist $num")})

    when:
    underTest.saveNewReleasesWithCover(releaseEntities)

    then:
    2 * underTest.coverDownloader.downloadAndSave(*_)
  }

  def "saveNewReleasesWithCover: should return all releases from cover downloader"() {
    given:
    def releaseEntities = []
    1.upto(BATCH_SIZE + 1, { num -> releaseEntities += ReleaseEntityFactory.createReleaseEntity("artist $num")})

    def release1 = ReleaseEntityFactory.createReleaseEntity("Sample 1")
    def release2 = ReleaseEntityFactory.createReleaseEntity("Sample 2")
    underTest.coverDownloader.downloadAndSave(*_) >>> [
        [release1],
        [release2]
    ]

    when:
    def result = underTest.saveNewReleasesWithCover(releaseEntities)

    then:
    result == [release1, release2]
  }

  def "finalizeImport: should return ImportResult with correct values for 'totalCountRequested' and 'totalCountImported'"() {
    when:
    def importResult = underTest.finalizeImport(2, 1)

    then:
    importResult == new ImportResult(totalCountRequested: 2, totalCountImported: 1)
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
    1 * underTest.threadPoolTaskExecutor.submit({ args ->
      args.releaseEntity == release1
      args.coverService == underTest.getCoverService()
    })

    then:
    0 * underTest.threadPoolTaskExecutor.submit(*_)
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
