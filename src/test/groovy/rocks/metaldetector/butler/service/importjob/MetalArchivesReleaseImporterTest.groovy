package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.MetalArchivesReleaseEntityConverter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesRestClient
import spock.lang.Specification
import spock.lang.Unroll

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES

class MetalArchivesReleaseImporterTest extends Specification {

  MetalArchivesReleaseImporter underTest = new MetalArchivesReleaseImporter(
          restClient: Mock(MetalArchivesRestClient),
          releaseEntityConverter: Mock(MetalArchivesReleaseEntityConverter),
          metalArchivesCoverService: Mock(CoverService),
          releaseRepository: Mock(ReleaseRepository)
  )

  def "rest client is called once on import"() {
    when:
    underTest.importReleases()

    then:
    1 * underTest.restClient.requestReleases() >> []
  }

  @Unroll
  "release converter is called for every response from rest template"() {
    given:
    underTest.restClient.requestReleases() >> releases

    when:
    underTest.importReleases()

    then:
    releases.size() * underTest.releaseEntityConverter.convert(_) >> []

    where:
    releases << [
        [],
        [new String[0], new String[0]]
    ]
  }

  def "should return METAL_ARCHIVES as release source"() {
    expect:
    underTest.getReleaseSource() == METAL_ARCHIVES
  }

  def "should return specific cover service"() {
    expect:
    underTest.metalArchivesCoverService == underTest.getCoverService()
  }
}
