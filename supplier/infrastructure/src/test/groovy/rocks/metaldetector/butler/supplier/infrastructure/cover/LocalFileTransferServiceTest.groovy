package rocks.metaldetector.butler.supplier.infrastructure.cover

import spock.lang.Specification

import static org.codehaus.groovy.runtime.ResourceGroovyMethods.write

class LocalFileTransferServiceTest extends Specification {

  LocalFileTransferService underTest = new LocalFileTransferService()
  File file

  void setup() {
    file = new File("classpath:test.txt")
    write(file, "someText")
  }

  void cleanup() {
    try {
      new File("classpath:test.txt").delete()
    }
    catch (Exception ignored) {}
    try {
      new File("classpath:test2.txt").delete()
    }
    catch (Exception ignored) {}
  }

  def "inputStream is written"() {
    given:
    def mockUrl = GroovyMock(URL)
    def inputStream = file.newInputStream()

    when:
    def result = underTest.transferFileFromUrl(mockUrl, "classpath:test2.txt")

    then:
    1 * mockUrl.openStream() >> inputStream

    and:
    result > 0
  }
}
