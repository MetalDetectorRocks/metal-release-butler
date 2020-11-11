package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient

import java.util.concurrent.Future

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES

@Service
@Slf4j
class MetalArchivesReleaseImporter implements ReleaseImporter {

  @Autowired
  MetalArchivesRestClient restClient

  @Autowired
  CoverService coverService

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  Converter<String[], List<ReleaseEntity>> releaseEntityConverter

  @Autowired
  ThreadPoolTaskExecutor releaseEntityPersistenceThreadPool

  @Override
  ImportResult importReleases() {
    // query metal archives
    def upcomingReleasesRawData = restClient.requestReleases()

    // convert raw string data into ReleaseEntity
    List<ReleaseEntity> releaseEntities = upcomingReleasesRawData.collectMany { releaseEntityConverter.convert(it) }.unique()

    // persist releases incl. cover download
    int inserted = persistReleaseEntities(releaseEntities)

    log.info("Import of new releases completed for Metal Archives!")

    return new ImportResult(
        totalCountRequested: upcomingReleasesRawData.size(),
        totalCountImported: inserted
    )
  }

  @Override
  ReleaseSource getReleaseSource() {
    return METAL_ARCHIVES
  }

  @Override
  void retryCoverDownload() {
    List<Future> futures = []
    releaseRepository.findAll()
        .findAll { releaseEntity ->
          releaseEntity.source == METAL_ARCHIVES && !releaseEntity.coverUrl
        }
        .each { releaseEntity ->
          futures << releaseEntityPersistenceThreadPool.submit(createPersistReleaseEntityTask(releaseEntity))
        }
    futures*.get()
  }

  private int persistReleaseEntities(List<ReleaseEntity> releaseEntities) {
    int inserted = 0
    List<Future> futures = []

    releaseEntities.each { ReleaseEntity releaseEntity ->
      if (!releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)) {
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
}
