package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.web.dto.ImportJobResponse

@Service
@Slf4j
class MetalHammerReleaseImportService implements ReleaseImportService {

  @Override
  ImportJobResponse importReleases() {
    throw new UnsupportedOperationException("not yet implemented")
  }
}
