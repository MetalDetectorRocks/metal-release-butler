package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileUrlResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.service.util.ProjectBasePathLocator

import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASE_IMAGES

@RestController
@RequestMapping(path = RELEASE_IMAGES)
@Profile(["default"])
class ReleaseCoverRestController {

  @Autowired
  ResourceLoader resourceLoader

  @Autowired
  ProjectBasePathLocator basePathLocator

  @GetMapping(path = "/{releaseCoverId}", produces = MediaType.IMAGE_JPEG_VALUE)
  ResponseEntity<Resource> getReleaseCover(@PathVariable String releaseCoverId) {
    String rootDir = basePathLocator.getResourceBasePath()
    Resource coverResource = new FileUrlResource("$rootDir/images/$releaseCoverId")
    return ResponseEntity.ok(coverResource)
  }
}
