package rocks.metaldetector.butler.service.cover

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@Service
class S3PersistenceService implements CoverPersistenceService {

  @Override
  String persistCover(URL coverUrl) {
    throw new UnsupportedOperationException("not yet implemented")
  }
}
