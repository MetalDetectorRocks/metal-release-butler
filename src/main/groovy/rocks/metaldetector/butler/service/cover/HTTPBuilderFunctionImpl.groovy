package rocks.metaldetector.butler.service.cover

import groovyx.net.http.HTTPBuilder
import org.springframework.stereotype.Component

@Component
class HTTPBuilderFunctionImpl implements HTTPBuilderFunction {

  @Override
  HTTPBuilder apply(URL albumUrl) {
    return new HTTPBuilder(albumUrl)
  }
}
