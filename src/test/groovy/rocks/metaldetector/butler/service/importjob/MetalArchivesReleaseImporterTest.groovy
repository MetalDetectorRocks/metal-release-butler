package rocks.metaldetector.butler.service.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.MetalArchivesReleaseEntityConverter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory.createReleaseEntity
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

class MetalArchivesReleaseImporterTest extends Specification {

  MetalArchivesReleaseImporter underTest = new MetalArchivesReleaseImporter(
      restClient: Mock(MetalArchivesRestClient),
      coverService: Mock(CoverService),
      releaseRepository: Mock(ReleaseRepository),
      releaseEntityConverter: Mock(MetalArchivesReleaseEntityConverter),
      releaseEntityPersistenceThreadPool: Mock(ThreadPoolTaskExecutor)
  )

  def "rest client is called once on import"() {
    when:
    underTest.importReleases()

    then:
    1 * underTest.restClient.requestReleases() >> []
  }

  @Unroll
  "release converter is called for every response from rest template"() {
    given:
    underTest.restClient.requestReleases() >> releases

    when:
    underTest.importReleases()

    then:
    releases.size() * underTest.releaseEntityConverter.convert(_) >> [new ReleaseEntity()]

    where:
    releases << [
        [],
        [new String[0], new String[0]]
    ]
  }

  def "Duplicates are filtered out before the database query checks whether the release already exists"() {
    given:
    underTest.restClient.requestReleases() >> [new String[0], new String[0], new String[0]]
    underTest.releaseEntityConverter.convert(_) >>> [
        [new ReleaseEntity(artist: "Darkthrone", albumTitle: "Transilvanian Hunger", releaseDate: LocalDate.of(1994, 10, 10))],
        [new ReleaseEntity(artist: "Darkthrone", albumTitle: "Transilvanian Hunger", releaseDate: LocalDate.of(1994, 10, 10))],
        [new ReleaseEntity(artist: "Darkthrone", albumTitle: "Panzerfaust", releaseDate: LocalDate.of(1994, 10, 10))],
    ]

    when:
    underTest.importReleases()

    then:
    2 * underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_)
  }

  def "new releases are submitted to persistence thread pool"() {
    given:
    underTest.restClient.requestReleases() >> [new String[0]]
    ReleaseEntity releaseEntity1 = createReleaseEntity("Metallica", LocalDate.now())
    ReleaseEntity releaseEntity2 = createReleaseEntity("Slayer", LocalDate.now())
    underTest.releaseEntityConverter.convert(_) >> [releaseEntity1, releaseEntity2]

    when:
    underTest.importReleases()

    then:
    2 * underTest.releaseEntityPersistenceThreadPool.submit(*_)
  }

  def "ReleaseEntity, CoverService and ReleaseRepository is passed to each created PersistReleaseEntityTask"() {
    given:
    underTest.restClient.requestReleases() >> [new String[0]]
    ReleaseEntity releaseEntity = createReleaseEntity("Metallica", LocalDate.now())
    underTest.releaseEntityConverter.convert(_) >> [releaseEntity]

    when:
    underTest.importReleases()

    then:
    1 * underTest.releaseEntityPersistenceThreadPool.submit({ args ->
      assert args.releaseEntity == releaseEntity
      assert args.coverService == underTest.coverService
      assert args.releaseRepository == underTest.releaseRepository
    })
  }

  def "existing releases are not submitted to persistence thread pool"() {
    given:
    underTest.restClient.requestReleases() >> [new String[0]]
    underTest.releaseEntityConverter.convert(_) >> [new ReleaseEntity()]

    when:
    underTest.importReleases()

    then:
    1 * underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_) >> true

    and:
    0 * underTest.releaseEntityPersistenceThreadPool.submit(_)
  }

  def "should return ImportResult with correct values for 'totalCountRequested' and 'totalCountImported'"() {
    given:
    underTest.restClient.requestReleases() >> [new String[0], new String[0]]
    underTest.releaseEntityConverter.convert(_) >>> [
        [createReleaseEntity("Metallica", LocalDate.now())],
        [createReleaseEntity("Slayer", LocalDate.now())]
    ]
    underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_) >>> [true, false]

    when:
    def importResult = underTest.importReleases()

    then:
    importResult == new ImportResult(totalCountRequested: 2, totalCountImported: 1)
  }

  def "should return METAL_ARCHIVES as release source"() {
    expect:
    underTest.getReleaseSource() == METAL_ARCHIVES
  }

  def "should call releaseRepository on retryCoverDownload"() {
    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.releaseRepository.findAll()
  }

  def "all metal archives releases without cover url are added to thread pool"() {
    given:
    def release1 = new ReleaseEntity(artist: "A", source: METAL_ARCHIVES)
    def release2 = new ReleaseEntity(artist: "B", source: METAL_HAMMER_DE)
    def release3 = new ReleaseEntity(artist: "C", source: METAL_ARCHIVES, coverUrl: "i'm an url")
    underTest.releaseRepository.findAll() >> [release1, release2, release3]

    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.releaseEntityPersistenceThreadPool.submit({ args ->
      assert args.releaseEntity == release1
      assert args.coverService == underTest.coverService
      assert args.releaseRepository == underTest.releaseRepository
    })

    then:
    0 * underTest.releaseEntityPersistenceThreadPool.submit(*_)
  }
}
