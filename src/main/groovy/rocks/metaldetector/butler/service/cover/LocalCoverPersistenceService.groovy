package rocks.metaldetector.butler.service.cover

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Slf4j
@Service
@Profile(["default"])
class LocalCoverPersistenceService implements CoverPersistenceService {

  static final String IMAGES_FOLDER_PATH = "images/"

  @Override
  String persistCover(URL coverUrl) {
    def success = createFolderIfNecessary()
    if (success) {
      def localPath = persistLocally(coverUrl)
      log.info("Transfer '${coverUrl}' to '${localPath}'")
      return localPath
    }
    else {
      log.warn("Could not persist cover from '${coverUrl}'")
      return null
    }
  }

  private boolean createFolderIfNecessary() {
    Path imagePath = Paths.get(IMAGES_FOLDER_PATH)
    if (!Files.exists(imagePath)) {
      try {
        Files.createDirectories(imagePath)
      }
      catch (IOException ex) {
        log.error("Error creating path: ${IMAGES_FOLDER_PATH}", ex)
        return false
      }
    }
    return true
  }

  private String persistLocally(URL coverUrl) {
    def imagePath = IMAGES_FOLDER_PATH + FilenameUtils.getName(coverUrl.getPath())
    ReadableByteChannel readableByteChannel = Channels.newChannel(coverUrl.openStream())
    FileOutputStream fileOutputStream = new FileOutputStream(imagePath)
    FileChannel fileChannel = fileOutputStream.getChannel()
    long bytesTransferred = fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
    return bytesTransferred > 0 ? imagePath : null
  }
}
