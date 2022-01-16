package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.service.release.ImageResourceFinder

import java.nio.file.Paths

import static org.springframework.http.MediaType.IMAGE_GIF_VALUE
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASE_IMAGES

@RestController
@RequestMapping(path = RELEASE_IMAGES)
@Profile(["default"])
class ReleaseCoverRestController {

  @Autowired
  ImageResourceFinder imageResourceFinder

  @GetMapping(produces = [IMAGE_JPEG_VALUE, IMAGE_GIF_VALUE, IMAGE_PNG_VALUE])
  ResponseEntity<Resource> getReleaseCover(@RequestParam String id) {
    Resource coverResource = imageResourceFinder.findImage(Paths.get("images", id))
    return ResponseEntity.ok(coverResource)
  }
}
