package rocks.metaldetector.butler.supplier.metalarchives.cover

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverFetcher
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverPersistenceService
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService

@Service
class MetalArchivesCoverService implements CoverService {

  @Autowired
  CoverFetcher metalArchivesCoverFetcher

  @Autowired
  CoverPersistenceService coverPersistenceService

  @Override
  String transfer(String sourceUrl, String targetFolder) {
    if (sourceUrl) {
      def coverUrl = metalArchivesCoverFetcher.fetchCoverUrl(sourceUrl)
      return coverUrl ? coverPersistenceService.persistCover(new URL(coverUrl), targetFolder) : null
    }
  }
}
