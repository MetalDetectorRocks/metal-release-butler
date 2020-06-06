package rocks.metaldetector.butler.service.cover

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PresignedUrlUploadRequest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class S3PersistenceService implements CoverPersistenceService {

  @Autowired
  AmazonS3 amazonS3Client

  @Override
  String persistCover(URL coverUrl) {
    // todo NilsD set content length (see AmazonS3.java)
    def uploadRequest = new PresignedUrlUploadRequest()
    uploadRequest.withInputStream(coverUrl.openStream())
    def uploadResult = amazonS3Client.upload(uploadRequest)
    uploadResult
  }
}
