package rocks.metaldetector.butler.supplier.timeformetal.cover

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverPersistenceService
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService

@Service
class TimeForMetalCoverService implements CoverService {

  @Autowired
  CoverPersistenceService coverPersistenceService

  @Override
  String transfer(String sourceUrl, String targetFolder) {
    if (sourceUrl) {
      def coverUrl = sourceUrl.replace("-100x100", "")
      return coverPersistenceService.persistCover(new URL(coverUrl), targetFolder)
    }
  }
}
