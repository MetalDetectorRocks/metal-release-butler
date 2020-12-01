package rocks.metaldetector.butler.service.cover

import spock.lang.Specification

class NoOpCoverServiceTest extends Specification {

  NoOpCoverService underTest = new NoOpCoverService();

  def "should return null for source url '#sourceUrl'"() {
    expect:
    underTest.transfer(sourceUrl) == null

    where:
    sourceUrl << [null, "", "source"]
  }
}
