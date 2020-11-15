package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.cover.CoverService

import java.util.concurrent.Future

@Slf4j
abstract class AbstractReleaseImporter {

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ThreadPoolTaskExecutor releaseEntityPersistenceThreadPool

  protected CoverService coverService

  abstract ImportResult importReleases()

  abstract ReleaseSource getReleaseSource()

  protected ImportResult persistReleaseEntities(List<ReleaseEntity> releaseEntities) {
    int inserted = 0
    List<Future> futures = []

    releaseEntities.unique().each { ReleaseEntity releaseEntity ->
      if (!releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)) {
        futures << releaseEntityPersistenceThreadPool.submit(createPersistReleaseEntityTask(releaseEntity))
        inserted++
      }
    }

    futures*.get()

    log.info("Import of new releases completed for ${getReleaseSource().name}!")

    return new ImportResult(
        totalCountRequested: releaseEntities.size(),
        totalCountImported: inserted
    )
  }

  protected void retryCoverDownload() {
    if (coverService) {
      List<Future> futures = []
      releaseRepository.findAll()
          .findAll { releaseEntity ->
            releaseEntity.source == getReleaseSource() && !releaseEntity.coverUrl
          }
          .each { releaseEntity ->
            futures << releaseEntityPersistenceThreadPool.submit(createPersistReleaseEntityTask(releaseEntity))
          }
      futures*.get()
    }
  }

  protected PersistReleaseEntityTask createPersistReleaseEntityTask(ReleaseEntity releaseEntity) {
    return new PersistReleaseEntityTask(
        releaseEntity: releaseEntity,
        coverService: coverService,
        releaseRepository: releaseRepository
    )
  }
}
