package rocks.metaldetector.butler.service.release

import org.springframework.context.ApplicationEvent

class ReleaseEntityDeleteRequestEvent extends ApplicationEvent {

  final String releaseDetailsUrl

  ReleaseEntityDeleteRequestEvent(Object source, String releaseDetailsUrl) {
    super(source)
    this.releaseDetailsUrl = releaseDetailsUrl
  }
}
