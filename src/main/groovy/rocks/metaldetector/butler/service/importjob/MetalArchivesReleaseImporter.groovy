package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES

@Service
@Slf4j
class MetalArchivesReleaseImporter extends AbstractReleaseImporter {

  @Autowired
  MetalArchivesRestClient restClient

  @Autowired
  CoverService metalArchivesCoverService

  @Autowired
  Converter<String[], List<ReleaseEntity>> releaseEntityConverter

  @Override
  ImportResult importReleases() {
    def upcomingReleasesRawData = restClient.requestReleases()
    List<ReleaseEntity> releaseEntities = upcomingReleasesRawData.collectMany { releaseEntityConverter.convert(it) }
    return finalizeImport(releaseEntities)
  }

  @Override
  ReleaseSource getReleaseSource() {
    return METAL_ARCHIVES
  }

  @Override
  CoverService getCoverService() {
    return metalArchivesCoverService
  }
}
