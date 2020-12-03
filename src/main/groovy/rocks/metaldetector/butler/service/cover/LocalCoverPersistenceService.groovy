package rocks.metaldetector.butler.service.cover

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static rocks.metaldetector.butler.config.constants.Endpoints.*

@Slf4j
@Service
@Profile(["default"])
class LocalCoverPersistenceService implements CoverPersistenceService {

  static final String IMAGES_FOLDER_PATH = "images/"

  @Autowired
  LocalFileTransferService fileTransferService

  @Override
  String persistCover(URL coverUrl) {
    try {
      createFolderIfNecessary()
      def localFileName = createLocalFileName(coverUrl)
      persistLocally(coverUrl, localFileName)
      return "$RELEASE_IMAGES/$localFileName"
    }
    catch (Exception e) {
      log.error("Could not persist cover from '${coverUrl}'", e)
      return null
    }
  }

  private void createFolderIfNecessary() {
    Path imagePath = Paths.get(IMAGES_FOLDER_PATH)
    if (!Files.exists(imagePath)) {
      Files.createDirectories(imagePath)
    }
  }

  private String createLocalFileName(URL coverUrl) {
    return UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(coverUrl.getPath())
  }

  private void persistLocally(URL coverUrl, String localFileName) {
    def localFilePath = IMAGES_FOLDER_PATH + localFileName
    log.info("Transfer '${coverUrl.toExternalForm()}' to '${localFilePath}'")
    long bytesTransferred = fileTransferService.transferFileFromUrl(coverUrl, localFilePath)
    if (bytesTransferred <= 0) {
      throw new RuntimeException("no bytes are transferred from $coverUrl")
    }
  }
}
