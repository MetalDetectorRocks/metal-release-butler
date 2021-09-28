package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import rocks.metaldetector.butler.web.api.Pagination
import spock.lang.Specification

import static rocks.metaldetector.butler.testutil.DtoFactory.ReleaseDtoFactory
import static rocks.metaldetector.butler.testutil.DtoFactory.ReleaseEntityFactory

class ReleasesResponseTransformerTest extends Specification {

  ReleasesResponseTransformer underTest = new ReleasesResponseTransformer(
          pageTransformer: Mock(PageTransformer),
          releaseTransformer: Mock(ReleaseTransformer)
  )

  def "transformPage: should call PageTransformer"() {
    given:
    def page = new PageImpl([], PageRequest.of(1, 10), 100)

    when:
    underTest.transformPage(page)

    then:
    1 * underTest.pageTransformer.transform(page)
  }

  def "transformPage: should return result from PageTransformer"() {
    given:
    def page = new PageImpl([], PageRequest.of(1, 10), 100)
    def pagination = new Pagination(currentPage: 1)
    underTest.pageTransformer.transform(page) >> pagination

    when:
    def result = underTest.transformPage(page)

    then:
    result.pagination == pagination
  }

  def "transformPage: should call ReleaseTransformer for each ReleaseEntity"() {
    given:
    def releaseEntity1 = ReleaseEntityFactory.createReleaseEntity("Karg")
    def releaseEntity2 = ReleaseEntityFactory.createReleaseEntity("Harakiri for the sky")
    def page = new PageImpl([releaseEntity1, releaseEntity2], PageRequest.of(1, 10), 100)

    when:
    underTest.transformPage(page)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntity1)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntity2)
  }

  def "transformPage: should return result from ReleaseTransformer"() {
    given:
    def releaseEntity1 = ReleaseEntityFactory.createReleaseEntity("Karg")
    def releaseEntity2 = ReleaseEntityFactory.createReleaseEntity("Harakiri for the sky")
    def releaseDto1 = ReleaseDtoFactory.createReleaseDto("Karg")
    def releaseDto2 = ReleaseDtoFactory.createReleaseDto("Harakiri for the sky")
    underTest.releaseTransformer.transform(releaseEntity1) >> releaseDto1
    underTest.releaseTransformer.transform(releaseEntity2) >> releaseDto2
    def page = new PageImpl([releaseEntity1, releaseEntity2], PageRequest.of(1, 10), 100)

    when:
    def result = underTest.transformPage(page)

    then:
    result.releases[0] == releaseDto1
    result.releases[1] == releaseDto2
  }

  def "transformReleaseEntities: should return pagination object with one page"() {
    given:
    def releaseEntities = [
            ReleaseEntityFactory.createReleaseEntity("Karg"),
            ReleaseEntityFactory.createReleaseEntity("Harakiri for the sky")
    ]

    when:
    def result = underTest.transformReleaseEntities(releaseEntities)

    then:
    result.pagination == new Pagination(
            currentPage: 1,
            size: releaseEntities.size(),
            totalPages: 1,
            totalReleases: releaseEntities.size()
    )
  }

  def "transformReleaseEntities: should call ReleaseTransformer for each ReleaseEntity"() {
    given:
    def releaseEntity1 = ReleaseEntityFactory.createReleaseEntity("Karg")
    def releaseEntity2 = ReleaseEntityFactory.createReleaseEntity("Harakiri for the sky")

    when:
    underTest.transformReleaseEntities([releaseEntity1, releaseEntity2])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntity1)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntity2)
  }

  def "transformReleaseEntities: should return result from ReleaseTransformer"() {
    given:
    def releaseEntity1 = ReleaseEntityFactory.createReleaseEntity("Karg")
    def releaseEntity2 = ReleaseEntityFactory.createReleaseEntity("Harakiri for the sky")
    def releaseDto1 = ReleaseDtoFactory.createReleaseDto("Karg")
    def releaseDto2 = ReleaseDtoFactory.createReleaseDto("Harakiri for the sky")
    underTest.releaseTransformer.transform(releaseEntity1) >> releaseDto1
    underTest.releaseTransformer.transform(releaseEntity2) >> releaseDto2

    when:
    def result = underTest.transformReleaseEntities([releaseEntity1, releaseEntity2])

    then:
    result.releases[0] == releaseDto1
    result.releases[1] == releaseDto2
  }
}
