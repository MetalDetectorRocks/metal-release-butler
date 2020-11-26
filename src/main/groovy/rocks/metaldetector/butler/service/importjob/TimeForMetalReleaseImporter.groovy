package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.timeformetal.TimeForMetalWebCrawler

import static rocks.metaldetector.butler.model.release.ReleaseSource.TIME_FOR_METAL

@Service
@Slf4j
class TimeForMetalReleaseImporter extends AbstractReleaseImporter {

  @Autowired
  TimeForMetalWebCrawler webCrawler

  @Autowired
  Converter<String, List<ReleaseEntity>> timeForMetalReleaseEntityConverter

  @Autowired
  @Qualifier("timeForMetalCoverService")
  CoverService coverService

  @Override
  ImportResult importReleases() {
    List<ReleaseEntity> releaseEntities = []
    def count = 1
    def newReleases = []
    do {
      def rawReleasesPage = webCrawler.requestReleases(count++)
      newReleases = timeForMetalReleaseEntityConverter.convert(rawReleasesPage)
      releaseEntities.addAll(newReleases)
    } while (newReleases)

    return persistReleaseEntities(releaseEntities)
  }

  @Override
  ReleaseSource getReleaseSource() {
    return TIME_FOR_METAL
  }
}