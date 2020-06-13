package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.scheduling.annotation.Async
import groovy.xml.XmlSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.web.dto.ImportJobResponse
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.supplier.metalhammer.MetalHammerRestClient
import rocks.metaldetector.butler.web.dto.ReleaseImportResponse

@Service
@Slf4j
class MetalHammerReleaseImportService implements ReleaseImportService {

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  MetalHammerRestClient restClient

  @Autowired
  Converter<String, List<ReleaseEntity>> metalHammerReleaseEntityConverter

  final XmlSlurper xmlSlurper

  MetalHammerReleaseImportService() {
    this.xmlSlurper = new XmlSlurper()
  }

  @Async
  @Override
  ImportJobResponse importReleases() {
    def rawReleasesPage = restClient.requestReleases()
    def releaseEntities = metalHammerReleaseEntityConverter.convert(rawReleasesPage)

    int inserted = 0
    releaseEntities.each { ReleaseEntity releaseEntity ->
      if (!releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)) {
        releaseRepository.save(releaseEntity)
        inserted++
      }
    }

    return new ReleaseImportResponse(totalCountRequested: releaseEntities.size(), totalCountImported: inserted)
  }
}
