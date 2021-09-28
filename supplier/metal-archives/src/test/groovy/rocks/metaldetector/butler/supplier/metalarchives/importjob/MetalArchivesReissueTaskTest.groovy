package rocks.metaldetector.butler.supplier.metalarchives.importjob

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.core.io.ClassPathResource
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesReleaseVersionsWebCrawler
import spock.lang.Specification
import spock.lang.Unroll

class MetalArchivesReissueTaskTest extends Specification {

  MetalArchivesReissueTask underTest = new MetalArchivesReissueTask(releaseEntity: new ReleaseEntity(releaseDetailsUrl: "url/666"),
                                                                    webCrawler: Mock(MetalArchivesReleaseVersionsWebCrawler))

  def "webCrawler is called with releaseId"() {
    when:
    underTest.run()

    then:
    underTest.webCrawler.requestOtherReleases("666") >> new Document("")
  }

  def "fetches distinct dates from document and sets reissue to true if more than one date exists"() {
    given:
    def document = Jsoup.parse(new ClassPathResource("mock-other-releases-page-reissue-metal-archives.txt").inputStream.text)
    underTest.webCrawler.requestOtherReleases("666") >> document

    when:
    underTest.run()

    then:
    underTest.releaseEntity.reissue
  }

  @Unroll
  "sets reissue to false otherwise"() {
    given:
    def document = Jsoup.parse(new ClassPathResource(mock).inputStream.text)
    underTest.webCrawler.requestOtherReleases("666") >> document

    when:
    underTest.run()

    then:
    !underTest.releaseEntity.reissue

    where:
    mock << ["mock-other-releases-page-no-reissue-1-metal-archives.txt",
             "mock-other-releases-page-no-reissue-2-metal-archives.txt"]
  }

  def "sets reissue to false is no document is given"() {
    given:
    underTest.webCrawler.requestOtherReleases(*_) >> null

    when:
    underTest.run()

    then:
    !underTest.releaseEntity.reissue
  }
}
