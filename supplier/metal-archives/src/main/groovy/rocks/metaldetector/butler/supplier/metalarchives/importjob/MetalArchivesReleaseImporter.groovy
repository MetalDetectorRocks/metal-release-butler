package rocks.metaldetector.butler.supplier.metalarchives.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource
import rocks.metaldetector.butler.supplier.infrastructure.converter.Converter
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService
import rocks.metaldetector.butler.supplier.infrastructure.importjob.AbstractReleaseImporter
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ImportResult
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesReleaseVersionsWebCrawler
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient

import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.METAL_ARCHIVES

@Service
@Slf4j
class MetalArchivesReleaseImporter extends AbstractReleaseImporter {

  @Autowired
  MetalArchivesRestClient restClient

  @Autowired
  CoverService metalArchivesCoverService

  @Autowired
  Converter<String[], List<ReleaseEntity>> releaseEntityConverter

  @Autowired
  MetalArchivesReleaseVersionsWebCrawler webCrawler

  @Override
  ImportResult importReleases() {
    def upcomingReleasesRawData = restClient.requestReleases()
    List<ReleaseEntity> releaseEntities = upcomingReleasesRawData.collectMany { releaseEntityConverter.convert(it) }
    List<ReleaseEntity> newReleaseEntities = saveNewReleasesWithCover(releaseEntities)

    def futures = newReleaseEntities.collect {
      threadPool.submit(createReissueTask(it))
    }

    futures*.get()
    releaseRepository.saveAll(newReleaseEntities)

    return finalizeImport(releaseEntities.size(), newReleaseEntities.size())
  }

  @Override
  ReleaseSource getReleaseSource() {
    return METAL_ARCHIVES
  }

  @Override
  CoverService getCoverService() {
    return metalArchivesCoverService
  }

  private MetalArchivesReissueTask createReissueTask(ReleaseEntity releaseEntity) {
    return new MetalArchivesReissueTask(
        releaseEntity: releaseEntity,
        webCrawler: webCrawler
    )
  }
}
