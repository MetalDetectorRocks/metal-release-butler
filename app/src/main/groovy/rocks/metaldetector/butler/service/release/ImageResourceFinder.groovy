package rocks.metaldetector.butler.service.release

import org.springframework.core.io.FileUrlResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class ImageResourceFinder {

  static final String[] VALID_FILE_EXTENSIONS = [".jpg", ".jpeg", ".png", ".gif", ".JPG", ".JPEG", ".PNG", ".GIF"]

  Optional<Resource> findImage(Path location) {
    Path path = Paths.get(System.getProperty("user.dir")).resolve(location)
    if (isValid(path)) {
      return Optional.of(new FileUrlResource(path.toUri().toURL()))
    }

    return Optional.empty()
  }

  private boolean isValid(Path path) {
    if (path.contains("..")) {
      throw new IllegalArgumentException("It's not allowed to move between folders. Please specify the complete path relative to the project directory.")
    }
    else if (!path.toString().endsWithAny(VALID_FILE_EXTENSIONS)) {
      throw new IllegalArgumentException("it's only allowed to load images with the following file extensions: $VALID_FILE_EXTENSIONS")
    }
    else if (Files.notExists(path)) {
      return false
    }

    return true
  }
}
