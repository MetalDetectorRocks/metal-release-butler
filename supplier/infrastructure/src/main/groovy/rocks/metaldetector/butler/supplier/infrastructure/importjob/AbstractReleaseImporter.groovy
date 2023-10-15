package rocks.metaldetector.butler.supplier.infrastructure.importjob

import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository

import java.util.concurrent.Future

@Slf4j
abstract class AbstractReleaseImporter implements ReleaseImporter {

  static final int BATCH_SIZE = 25
  private static final Comparator<ReleaseEntity> RELEASE_ENTITY_COMPARATOR = { release1, release2 ->
    release1.artist.toLowerCase() <=> release2.artist.toLowerCase()
  }

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ParallelCoverDownloader coverDownloader

  @Autowired
  ThreadPoolTaskExecutor threadPoolTaskExecutor

  @Override
  @Transactional
  void retryCoverDownload() {
    List<Future> futures = []
    def releaseEntitiesToUpdate = releaseRepository.findAll()
        .findAll { releaseEntity ->
          releaseEntity.source == getReleaseSource() && !releaseEntity.coverUrl
        }
        .each { releaseEntity ->
          futures << threadPoolTaskExecutor.submit(createCoverDownloadTask(releaseEntity))
        }
        .collect()

    log.info("Reload covers for ${releaseEntitiesToUpdate.size()} entries...")
    futures*.get()
    releaseRepository.saveAll(releaseEntitiesToUpdate)
  }

  @Synchronized
  List<ReleaseEntity> saveNewReleasesWithCover(List<ReleaseEntity> releaseEntities) {
    def newReleases = preFilter(releaseEntities)
    log.info("Out of ${releaseEntities.size()} requested releases, ${newReleases.size()} new releases are imported")

    int batch = 0
    List<ReleaseEntity> savedReleaseEntities = []
    List<List<ReleaseEntity>> releaseBatches = newReleases.collate(BATCH_SIZE)
    for (List<ReleaseEntity> releaseBatch : releaseBatches) {
      log.info("Transfer covers for batch ${++batch}/${releaseBatches.size()}")
      savedReleaseEntities += coverDownloader.downloadAndSave(releaseBatch, releaseEntity -> createCoverDownloadTask(releaseEntity))
      log.info("Releases for batch ${batch}/${releaseBatches.size()} successfully saved.")
    }

    return savedReleaseEntities
  }

  private List<ReleaseEntity> preFilter(List<ReleaseEntity> releaseEntities) {
    return releaseEntities.unique(false, RELEASE_ENTITY_COMPARATOR)
        .findAll { releaseEntity ->
          !releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)
        }
        .collect()
  }

  protected ImportResult finalizeImport(int totalCountRequested, int totalCountImported) {
    log.info("Import of new releases completed for ${getReleaseSource().displayName}. " +
            "${totalCountImported} out of ${totalCountRequested} requested releases were imported.")
    return new ImportResult(
        totalCountRequested: totalCountRequested,
        totalCountImported: totalCountImported
    )
  }

  protected CoverDownloadTask createCoverDownloadTask(ReleaseEntity releaseEntity) {
    return new CoverDownloadTask(
        releaseEntity: releaseEntity,
        coverService: getCoverService()
    )
  }
}
