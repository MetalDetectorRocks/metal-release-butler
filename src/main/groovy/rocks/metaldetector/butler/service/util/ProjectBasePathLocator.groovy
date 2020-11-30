package rocks.metaldetector.butler.service.util

import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils

@Service // ToDo DanielW: Tests
class ProjectBasePathLocator {

  String getResourceBasePath() {
    File path = new File(ResourceUtils.getURL("classpath:").getPath())
    if (path == null || !path.exists()) {
      path = new File("")
    }
    String absolutePath = path.getAbsolutePath()
    return absolutePath.replace("/out/production/classes", "")
  }
}
