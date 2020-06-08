package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.service.transformer.ImportJobTransformer
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient
import rocks.metaldetector.butler.web.dto.ImportJobResponse

import java.time.LocalDateTime
import java.util.concurrent.Future

@Service
@Slf4j
class MetalArchivesReleaseImportService implements ReleaseImportService {

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ImportJobRepository importJobRepository

  @Autowired
  MetalArchivesRestClient restClient

  @Autowired
  CoverService coverService

  @Autowired
  Converter<String[], List<ReleaseEntity>> releaseEntityConverter

  @Autowired
  ThreadPoolTaskExecutor releaseEntityPersistenceThreadPool

  @Autowired
  ImportJobTransformer importJobTransformer

  @Override
  @Async
  ImportJobResponse importReleases(Long internalJobId) {
    // query metal archives
    def upcomingReleasesRawData = restClient.requestReleases()

    // convert raw string data into ReleaseEntity
    List<ReleaseEntity> releaseEntities = upcomingReleasesRawData.collectMany { releaseEntityConverter.convert(it) }

    // persist releases incl. cover download
    int inserted = persistReleaseEntities(releaseEntities)

    // update import job
    ImportJobEntity importJobEntity = updateImportJob(internalJobId, upcomingReleasesRawData.size(), inserted)

    return importJobTransformer.transform(importJobEntity)
  }

  private int persistReleaseEntities(List<ReleaseEntity> releaseEntities) {
    int inserted = 0
    List<Future> futures = []

    releaseEntities.each { ReleaseEntity releaseEntity ->
      if (!releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)) {
        futures << releaseEntityPersistenceThreadPool.submit(createPersistReleaseEntityTask(releaseEntity))
        inserted++
      }
    }

    futures*.get()
    return inserted
  }

  private PersistReleaseEntityTask createPersistReleaseEntityTask(ReleaseEntity releaseEntity) {
    return new PersistReleaseEntityTask(
            releaseEntity: releaseEntity,
            coverService: coverService,
            releaseRepository: releaseRepository
    )
  }

  private ImportJobEntity updateImportJob(Long internalJobId, int totalCountRequested, int totalCountImported) {
    ImportJobEntity importJobEntity = importJobRepository.findById(internalJobId).get()
    importJobEntity.totalCountRequested = totalCountRequested
    importJobEntity.totalCountImported = totalCountImported
    importJobEntity.endTime = LocalDateTime.now()

    return importJobRepository.save(importJobEntity)
  }
}
