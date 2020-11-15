package rocks.metaldetector.butler.service.cover

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TimeForMetalCoverService implements CoverService {

  @Autowired
  CoverPersistenceService coverPersistenceService

  @Override
  String transfer(String sourceUrl) {
    if (sourceUrl) {
      def coverUrl = sourceUrl.replace("-100x100", "")
      return coverPersistenceService.persistCover(new URL(coverUrl))
    }
  }
}
