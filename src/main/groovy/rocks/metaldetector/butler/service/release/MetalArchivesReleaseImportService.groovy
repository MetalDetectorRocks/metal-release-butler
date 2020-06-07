package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient
import rocks.metaldetector.butler.web.dto.ImportJobResponse

import java.util.concurrent.Future

@Service
@Slf4j
class MetalArchivesReleaseImportService implements ReleaseImportService {

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  MetalArchivesRestClient restClient

  @Autowired
  CoverService coverService

  @Autowired
  Converter<String[], List<ReleaseEntity>> releaseEntityConverter

  @Autowired
  ThreadPoolTaskExecutor releaseEntityPersistenceThreadPool

  @Override
  ImportJobResponse importReleases() {
    // query metal archives
    def upcomingReleasesRawData = restClient.requestReleases()

    // convert raw string data into ReleaseEntity
    List<ReleaseEntity> releaseEntities = upcomingReleasesRawData.collectMany { releaseEntityConverter.convert(it) }

    // insert new releases
    int inserted = 0
    List<Future> futures = []

    releaseEntities.each { ReleaseEntity releaseEntity ->
      if (!releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)) {
        futures << releaseEntityPersistenceThreadPool.submit(createPersistReleaseEntityTask(releaseEntity))
        inserted++
      }
    }

    futures*.get()
    // ToDo DanielW: Handle Return Value
    return new ImportJobResponse(totalCountRequested: upcomingReleasesRawData.size(), totalCountImported: inserted)
  }

  private PersistReleaseEntityTask createPersistReleaseEntityTask(ReleaseEntity releaseEntity) {
    return new PersistReleaseEntityTask(
            releaseEntity: releaseEntity,
            coverService: coverService,
            releaseRepository: releaseRepository
    )
  }
}
