package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter
import rocks.metaldetector.butler.supplier.metalhammer.MetalHammerWebCrawler
import spock.lang.Specification

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

class MetalHammerReleaseImporterTest extends Specification {

  MetalHammerReleaseImporter underTest = new MetalHammerReleaseImporter(
      webCrawler: Mock(MetalHammerWebCrawler),
      metalHammerReleaseEntityConverter: Mock(MetalHammerReleaseEntityConverter),
      releaseRepository: Mock(ReleaseRepository)
  )

  def "web crawler is called"() {
    given:
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.webCrawler.requestReleases() >> "page"
  }

  def "releaseEntityConverter is called with web crawlers response"() {
    given:
    def responsePage = "page"
    underTest.webCrawler.requestReleases() >> responsePage

    when:
    underTest.importReleases()

    then:
    1 * underTest.metalHammerReleaseEntityConverter.convert(responsePage) >> []
  }

  def "should return METAL_HAMMER_DE as release source"() {
    expect:
    underTest.getReleaseSource() == METAL_HAMMER_DE
  }
}
