package rocks.metaldetector.butler.service.cover

import groovyx.net.http.HTTPBuilder
import org.springframework.stereotype.Component

@Component
class HTTPBuilderFunctionImpl implements HTTPBuilderFunction {

  @Override
  HTTPBuilder apply(String albumUrl) {
    return new HTTPBuilder(albumUrl)
  }
}
