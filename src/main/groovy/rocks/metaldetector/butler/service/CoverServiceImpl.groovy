package rocks.metaldetector.butler.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import org.apache.commons.io.FilenameUtils
import org.springframework.stereotype.Component
import rocks.metaldetector.butler.model.release.ReleaseEntity

import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
@Slf4j
class CoverServiceImpl implements CoverService {

  static final String IMAGES_FOLDER_PATH = "images/"
  static final String ALBUM_COVER_HTML_ID = "cover"

  @Override
  void downloadReleaseCover(ReleaseEntity releaseEntity) {
    if (releaseEntity?.metalArchivesAlbumUrl) {
      def albumCoverLink = fetchCoverLink(releaseEntity.metalArchivesAlbumUrl)
      def folderCreated = createFolderIfNecessary()
      if (albumCoverLink && folderCreated) {
        def coverPath = downloadCover(albumCoverLink)
        releaseEntity.coverUrl = coverPath
      }
    }
    else {
      log.info("No album url given for '${releaseEntity?.albumTitle}' from '${releaseEntity.artist}'")
    }
  }

  private URL fetchCoverLink(URL releasePageLink) {
    HTTPBuilder httpBuilder = new HTTPBuilder(releasePageLink)
    def releasePage = httpBuilder.get([:])
    def coverDiv = releasePage?."**"?.findAll { it.@id == ALBUM_COVER_HTML_ID }?.first()
    def coverLink = coverDiv?.@href?.text() as String
    return coverLink ? new URL(coverLink) : null
  }

  private String downloadCover(URL albumCoverUrl) {
    def imagePath = IMAGES_FOLDER_PATH + FilenameUtils.getName(albumCoverUrl.getPath())
    ReadableByteChannel readableByteChannel = Channels.newChannel(albumCoverUrl.openStream())
    FileOutputStream fileOutputStream = new FileOutputStream(imagePath)
    FileChannel fileChannel = fileOutputStream.getChannel()
    long bytesTransferred = fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
    return bytesTransferred > 0 ? imagePath : null
  }

  private boolean createFolderIfNecessary() {
    Path imagePath = Paths.get(IMAGES_FOLDER_PATH)
    if (!Files.exists(imagePath)) {
      try {
        Files.createDirectories(imagePath)
      }
      catch (IOException ex) {
        log.warn("Error creating path: '" + IMAGES_FOLDER_PATH + "'", ex)
        return false
      }
    }
    return true
  }
}