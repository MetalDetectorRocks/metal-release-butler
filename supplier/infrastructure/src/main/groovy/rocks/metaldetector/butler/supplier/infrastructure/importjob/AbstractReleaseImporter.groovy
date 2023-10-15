package rocks.metaldetector.butler.supplier.infrastructure.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository

import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantReadWriteLock

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

  @Autowired
  ReentrantReadWriteLock reentrantReadWriteLock

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

  @Transactional
  List<ReleaseEntity> saveNewReleasesWithCover(List<ReleaseEntity> releaseEntities) {
    def newReleases = preFilter(releaseEntities)
    int batch = 0
    List<ReleaseEntity> savedReleaseEntities = []
    List<List<ReleaseEntity>> releaseBatches = newReleases.collate(BATCH_SIZE)
    for (List<ReleaseEntity> releaseBatch : releaseBatches) {
      log.info("Transfer covers for batch ${++batch}/${releaseBatches.size()}")
      coverDownloader.download(releaseBatch, releaseEntity -> createCoverDownloadTask(releaseEntity))
      savedReleaseEntities += save(releaseBatch)
      log.info("Releases for batch ${batch}/${releaseBatches.size()} successfully saved.")
    }

    log.info("Out of ${releaseEntities.size()} requested releases, ${savedReleaseEntities.size()} new releases are imported")

    return savedReleaseEntities
  }

  private List<ReleaseEntity> preFilter(List<ReleaseEntity> releaseEntities) {
    def uniqueReleases = releaseEntities.unique(false, RELEASE_ENTITY_COMPARATOR)
    reentrantReadWriteLock.readLock().lock()
    try {
      return uniqueReleases
          .findAll { releaseEntity ->
            !releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)
          }
    }
    finally {
      reentrantReadWriteLock.readLock().unlock()
    }
  }

  private List<ReleaseEntity> save(List<ReleaseEntity> releases) {
    reentrantReadWriteLock.writeLock().lock()
    try {
      releases = releases.findAll { releaseEntity ->
        !releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)
      }
      return releaseRepository.saveAll(releases)
    }
    finally {
      reentrantReadWriteLock.writeLock().unlock()
    }
  }

  protected ImportResult finalizeImport(int totalCountRequested, int totalCountImported) {
    log.info("Import of new releases completed for ${getReleaseSource().displayName}. ${totalCountImported} out of ${totalCountRequested} requested releases were imported.")
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
