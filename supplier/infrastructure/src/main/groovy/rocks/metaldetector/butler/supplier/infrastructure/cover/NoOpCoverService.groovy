package rocks.metaldetector.butler.supplier.infrastructure.cover

import org.springframework.stereotype.Service

@Service
class NoOpCoverService implements CoverService {

  @Override
  String transfer(String sourceUrl, String targetFolder) {
    return null
  }
}
