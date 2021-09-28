package rocks.metaldetector.butler.supplier.infrastructure.cover

import spock.lang.Specification

class NoOpCoverServiceTest extends Specification {

  NoOpCoverService underTest = new NoOpCoverService();

  def "should return null for source url '#sourceUrl'"() {
    expect:
    underTest.transfer(sourceUrl, "path/to/target") == null

    where:
    sourceUrl << [null, "", "source"]
  }
}
