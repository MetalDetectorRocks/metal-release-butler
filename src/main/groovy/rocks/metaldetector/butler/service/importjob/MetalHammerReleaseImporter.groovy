package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.supplier.metalhammer.MetalHammerWebCrawler

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

@Service
@Slf4j
class MetalHammerReleaseImporter extends AbstractReleaseImporter {

  @Autowired
  MetalHammerWebCrawler webCrawler

  @Autowired
  Converter<String, List<ReleaseEntity>> metalHammerReleaseEntityConverter

  @Override
  ImportResult importReleases() {
    def rawReleasesPage = webCrawler.requestReleases()
    def releaseEntities = metalHammerReleaseEntityConverter.convert(rawReleasesPage)
    return persistReleaseEntities(releaseEntities)
  }

  @Override
  ReleaseSource getReleaseSource() {
    return METAL_HAMMER_DE
  }
}
