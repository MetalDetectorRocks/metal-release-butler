package rocks.metaldetector.butler.service.cover

import groovyx.net.http.HTTPBuilder
import spock.lang.Specification

class HTTPBuilderFunctionImplTest extends Specification {

  HTTPBuilderFunctionImpl underTest = new HTTPBuilderFunctionImpl()

  def "returns new HTTPBuilder instance with given uri"() {
    given:
    def url = "http://www.internet.com"

    when:
    HTTPBuilder result = underTest.apply(url)

    then:
    result.uri as String == url
  }
}
