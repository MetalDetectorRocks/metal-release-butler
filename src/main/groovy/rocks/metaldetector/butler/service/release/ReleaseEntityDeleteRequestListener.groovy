package rocks.metaldetector.butler.service.release

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class ReleaseEntityDeleteRequestListener implements ApplicationListener<ReleaseEntityDeleteRequestEvent> {

  @Autowired
  ReleaseService releaseService

  @Override
  void onApplicationEvent(ReleaseEntityDeleteRequestEvent event) {
    releaseService.deleteByReleaseDetailsUrl(event.releaseDetailsUrl)
  }
}
