package rocks.metaldetector.butler.supplier.infrastructure.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository

import java.util.concurrent.Future

@Slf4j
abstract class AbstractReleaseImporter implements ReleaseImporter {

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ThreadPoolTaskExecutor threadPool

  @Override
  void retryCoverDownload() {
    List<Future> futures = []
    def releaseEntitiesToUpdate = releaseRepository.findAll()
            .findAll { releaseEntity ->
              releaseEntity.source == getReleaseSource() && !releaseEntity.coverUrl
            }
            .each { releaseEntity ->
              futures << threadPool.submit(createCoverTransferTask(releaseEntity))
            }
            .collect()

    futures*.get()
    releaseRepository.saveAll(releaseEntitiesToUpdate)
  }

  protected List<ReleaseEntity> saveNewReleasesWithCover(List<ReleaseEntity> releaseEntities) {
    List<Future> futures = []
    Comparator<ReleaseEntity> releaseEntityComparator = {release1, release2 ->
      release1.artist.toLowerCase() <=> release2.artist.toLowerCase()
    }
    def releaseEntitiesToSave = releaseEntities.unique(false, releaseEntityComparator)
            .findAll {releaseEntity ->
              !releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)
            }
            .each {releaseEntity ->
              futures << threadPool.submit(createCoverTransferTask(releaseEntity))
            }
            .collect()

    futures*.get()
    return releaseRepository.saveAll(releaseEntitiesToSave)
  }

  protected ImportResult finalizeImport(int totalCountRequested, int totalCountImported) {
    log.info("Import of new releases completed for ${getReleaseSource().displayName}!")
    return new ImportResult(
        totalCountRequested: totalCountRequested,
        totalCountImported: totalCountImported
    )
  }

  protected CoverTransferTask createCoverTransferTask(ReleaseEntity releaseEntity) {
    return new CoverTransferTask(
        releaseEntity: releaseEntity,
        coverService: getCoverService()
    )
  }
}
