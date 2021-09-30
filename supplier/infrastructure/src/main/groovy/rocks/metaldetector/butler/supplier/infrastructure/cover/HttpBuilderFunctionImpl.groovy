package rocks.metaldetector.butler.supplier.infrastructure.cover

import groovyx.net.http.HttpBuilder
import org.springframework.stereotype.Component

@Component
class HttpBuilderFunctionImpl implements HttpBuilderFunction {

  @Override
  HttpBuilder apply(String albumUrl) {
    return HttpBuilder.configure {
      request.uri = albumUrl
    }
  }
}