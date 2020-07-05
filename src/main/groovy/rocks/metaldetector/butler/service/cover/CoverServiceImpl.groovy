package rocks.metaldetector.butler.service.cover

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class CoverServiceImpl implements CoverService {

  @Autowired
  CoverFetcher metalArchivesCoverFetcher

  @Autowired
  CoverPersistenceService coverPersistenceService

  @Override
  String transfer(URL sourceUrl) {
    def coverUrl = metalArchivesCoverFetcher.fetchCoverUrl(sourceUrl)
    return coverUrl ? coverPersistenceService.persistCover(coverUrl) : null
  }
}
