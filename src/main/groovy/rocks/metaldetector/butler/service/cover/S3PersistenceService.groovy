package rocks.metaldetector.butler.service.cover

import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Slf4j
@Service
@Profile(["preview", "prod"])
class S3PersistenceService implements CoverPersistenceService {

  static final String BUCKET_NAME = "rocks.metal-detector.s3.images"
  static final String PATH = "images/"

  @Autowired
  AmazonS3 amazonS3Client

  @Override
  String persistCover(URL coverUrl) {
    def key = PATH + FilenameUtils.getName(coverUrl.getPath())
    try {
      amazonS3Client.putObject(BUCKET_NAME, key, coverUrl.openStream(), null)
      return key
    }
    catch (SdkClientException ex) {
      log.warn("Could not upload cover from '${coverUrl}'", ex)
      return null
    }
  }
}
