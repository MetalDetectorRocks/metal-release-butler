package rocks.metaldetector.butler.supplier.infrastructure.cover

import groovyx.net.http.HttpBuilder
import spock.lang.Specification

class HttpBuilderFunctionImplTest extends Specification {

  HttpBuilderFunctionImpl underTest = new HttpBuilderFunctionImpl()

  def "returns new HttpBuilder instance with given uri"() {
    given:
    def url = "http://www.internet.com"

    when:
    HttpBuilder result = underTest.apply(url)

    then:
    "$result.config.request.uri.scheme://$result.config.request.uri.host" == url
  }
}
