package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.web.dto.ImportJobResponse

@Service
@Slf4j
class MetalHammerReleaseImportService implements ReleaseImportService {

  @Async
  @Override
  ImportJobResponse importReleases(Long internalJobId) {
    throw new UnsupportedOperationException("not yet implemented")
  }
}
