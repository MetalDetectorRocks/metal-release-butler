package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.service.cover.ImageResourceFinder

import java.nio.file.Paths

import static org.springframework.http.MediaType.*
import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASE_IMAGES

@RestController
@RequestMapping(path = RELEASE_IMAGES)
@Profile(["default"])
class ReleaseCoverRestController {

  @Autowired
  ImageResourceFinder imageResourceFinder

  @GetMapping(path = "/{releaseCoverId}", produces = [IMAGE_JPEG_VALUE, IMAGE_GIF_VALUE, IMAGE_PNG_VALUE])
  ResponseEntity<Resource> getReleaseCover(@PathVariable String releaseCoverId) {
    Optional<Resource> coverResource = imageResourceFinder.findImage(Paths.get("images", releaseCoverId))
    return ResponseEntity.of(coverResource)
  }
}
