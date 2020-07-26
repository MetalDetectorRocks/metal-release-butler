package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.supplier.metalhammer.MetalHammerWebCrawler

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

@Service
@Slf4j
class MetalHammerReleaseImporter implements ReleaseImporter {

  @Autowired
  MetalHammerWebCrawler webCrawler

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  Converter<String, List<ReleaseEntity>> metalHammerReleaseEntityConverter

  @Override
  ImportResult importReleases() {
    def rawReleasesPage = webCrawler.requestReleases()
    def releaseEntities = metalHammerReleaseEntityConverter.convert(rawReleasesPage)
    int totalCountRequested = releaseEntities.size()

    int inserted = 0
    releaseEntities.unique().each { ReleaseEntity releaseEntity ->
      if (!releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(releaseEntity.artist, releaseEntity.albumTitle, releaseEntity.releaseDate)) {
        releaseRepository.save(releaseEntity)
        inserted++
      }
    }

    log.info("Import of new releases completed for Metal Hammer!")

    return new ImportResult(
            totalCountRequested: totalCountRequested,
            totalCountImported: inserted
    )
  }

  @Override
  ReleaseSource getReleaseSource() {
    return METAL_HAMMER_DE
  }

  @Override
  void retryCoverDownload() {
    // do nothing, no covers for metal hammer available (yet)
  }
}
