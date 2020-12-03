package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository

import java.util.concurrent.Future

@Slf4j
abstract class AbstractReleaseImporter implements ReleaseImporter {

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ThreadPoolTaskExecutor coverTransferThreadPool

  @Override
  void retryCoverDownload() {
    List<Future> futures = []
    def releaseEntitiesToUpdate = releaseRepository.findAll()
            .findAll { releaseEntity ->
              releaseEntity.source == getReleaseSource() && !releaseEntity.coverUrl
            }
            .each { releaseEntity ->
              futures << coverTransferThreadPool.submit(createCoverTransferTask(releaseEntity))
            }
            .collect()

    futures*.get()
    releaseRepository.saveAll(releaseEntitiesToUpdate)
  }

  protected ImportResult finalizeImport(List<ReleaseEntity> releaseEntities) {
    List<Future> futures = []
    def releaseEntitiesToSave = releaseEntities.unique(false)
            .findAll {releaseEntity ->
              !releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)
            }
            .each {releaseEntity ->
              futures << coverTransferThreadPool.submit(createCoverTransferTask(releaseEntity))
            }
            .collect()

    futures*.get()
    releaseRepository.saveAll(releaseEntitiesToSave)

    log.info("Import of new releases completed for ${getReleaseSource().displayName}!")

    return new ImportResult(
        totalCountRequested: releaseEntities.size(),
        totalCountImported: releaseEntitiesToSave.size()
    )
  }

  protected CoverTransferTask createCoverTransferTask(ReleaseEntity releaseEntity) {
    return new CoverTransferTask(
        releaseEntity: releaseEntity,
        coverService: getCoverService()
    )
  }
}
