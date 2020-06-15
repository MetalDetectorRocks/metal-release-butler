package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.service.cover.HTTPBuilderFunction
import rocks.metaldetector.butler.supplier.metalhammer.MetalHammerWebCrawler
import rocks.metaldetector.butler.web.dto.ImportJobResponse

@Service
@Slf4j
class MetalHammerReleaseImporter extends ReleaseImporter {

  @Autowired
  MetalHammerWebCrawler restClient

  @Autowired
  HTTPBuilderFunction httpBuilderFunction

  @Autowired
  Converter<String, List<ReleaseEntity>> metalHammerReleaseEntityConverter

  final XmlSlurper xmlSlurper

  MetalHammerReleaseImporter() {
    this.xmlSlurper = new XmlSlurper()
  }

  @Async
  @Override
  ImportJobResponse importReleases(Long internalJobId) {
    def rawReleasesPage = restClient.requestReleases()
    def releaseEntities = metalHammerReleaseEntityConverter.convert(rawReleasesPage)

    int inserted = 0
    releaseEntities.each { ReleaseEntity releaseEntity ->
      if (!releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)) {
        releaseRepository.save(releaseEntity)
        inserted++
      }
    }

    ImportJobEntity importJobEntity = updateImportJob(internalJobId, releaseEntities.size(), inserted)

    log.info("Import of new releases completed for Metal Hammer!")
    return importJobTransformer.transform(importJobEntity)
  }
}
