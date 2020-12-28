package rocks.metaldetector.butler.service.cover

import org.springframework.stereotype.Service

@Service
class NoOpCoverService implements CoverService {

  @Override
  String transfer(String sourceUrl, String targetFolder) {
    return null
  }
}
