package rocks.metaldetector.butler.service.release

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.ReleaseEntityConverter
import rocks.metaldetector.butler.service.transformer.ImportJobTransformer
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient
import rocks.metaldetector.butler.web.dto.ImportJobResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory.createReleaseEntity

class MetalArchivesReleaseImportServiceTest extends Specification {

  MetalArchivesReleaseImportService underTest = new MetalArchivesReleaseImportService(
          releaseRepository: Mock(ReleaseRepository),
          importJobRepository: Mock(ImportJobRepository),
          restClient: Mock(MetalArchivesRestClient),
          releaseEntityConverter: Mock(ReleaseEntityConverter),
          releaseEntityPersistenceThreadPool: Mock(ThreadPoolTaskExecutor),
          importJobTransformer: Mock(ImportJobTransformer)
  )

  def setup() {
    underTest.importJobRepository.findById(_) >> Optional.of(new ImportJobEntity())
  }

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

  def "should update the corresponding import job"() {
    given:
    def internalJobId = 666
    def importJobEntityMock = new ImportJobEntity()
    underTest.restClient.requestReleases() >> [new String[0]]
    underTest.releaseEntityConverter.convert(_) >> [new ReleaseEntity()]

    when:
    underTest.importReleases(internalJobId)

    then:
    1 * underTest.importJobRepository.findById(internalJobId) >> Optional.of(importJobEntityMock)

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.totalCountRequested == 1
      assert args.totalCountImported == 1
      assert args.endTime
    })
  }

  def "should update import job with correct values for 'totalCountRequested' and 'totalCountImported'"() {
    given:
    underTest.restClient.requestReleases() >> [new String[0], new String[0]]
    underTest.releaseEntityConverter.convert(_) >> [
            createReleaseEntity("Metallica", LocalDate.now())
    ]
    underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_) >>> [true, false]

    when:
    underTest.importReleases()

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.totalCountRequested == 2
      assert args.totalCountImported == 1
      assert args.endTime
    })
  }

  def "should use ImportJobTransformer to transform ImportJobEntity that is used as result"() {
    given:
    def importJobEntityMock = new ImportJobEntity()
    def transformedResponse = new ImportJobResponse()
    underTest.restClient.requestReleases() >> [new String[0]]
    underTest.releaseEntityConverter.convert(*_) >> [new ReleaseEntity()]
    underTest.importJobRepository.save(*_) >> importJobEntityMock

    when:
    def result = underTest.importReleases()

    then:
    1 * underTest.importJobTransformer.transform(importJobEntityMock) >> transformedResponse

    and:
    result == transformedResponse
  }
}
