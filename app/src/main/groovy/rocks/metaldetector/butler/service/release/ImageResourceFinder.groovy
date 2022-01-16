package rocks.metaldetector.butler.service.release

import org.springframework.core.io.FileUrlResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.config.web.ResourceNotFoundException

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class ImageResourceFinder {

  static final String ERROR_MESSAGE_DOTS = "It's not allowed to move between folders. Please specify the complete path relative to the project directory."
  static final String ERROR_MESSAGE_EXTENSION = "it's only allowed to load images with the following file extensions: "
  static final String ERROR_MESSAGE_NOT_FOUND = "Image not found"
  static final String[] VALID_FILE_EXTENSIONS = [".jpg", ".jpeg", ".png", ".gif", ".JPG", ".JPEG", ".PNG", ".GIF"]

  Resource findImage(Path location) {
    Path path = Paths.get(System.getProperty("user.dir")).resolve(location)
    validatePath(path)
    return new FileUrlResource(path.toUri().toURL())
  }

  private void validatePath(Path path) {
    if (path.toString().contains("..")) {
      throw new ResourceNotFoundException(ERROR_MESSAGE_DOTS)
    }
    if (!path.toString().endsWithAny(VALID_FILE_EXTENSIONS)) {
      throw new ResourceNotFoundException("$ERROR_MESSAGE_EXTENSION$VALID_FILE_EXTENSIONS")
    }
    if (Files.notExists(path)) {
      throw new ResourceNotFoundException(ERROR_MESSAGE_NOT_FOUND)
    }
  }
}
