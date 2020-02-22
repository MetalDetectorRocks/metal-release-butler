package rocks.metaldetector.butler.model.release

import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.*

@Tag("integration-test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DataJpaTest
class ReleaseRepositoryIT extends Specification {

  @Autowired
  ReleaseRepository underTest

  static ReleaseEntity release1 = ReleaseEntityFactory.createReleaseEntity("A1", LocalDate.of(2020, 1, 1))
  static ReleaseEntity release2 = ReleaseEntityFactory.createReleaseEntity("A2", LocalDate.of(2020, 2, 1))
  static ReleaseEntity release3 = ReleaseEntityFactory.createReleaseEntity("A3", LocalDate.of(2020, 3, 1))

  void setup() {
    def releases = [release1, release2, release3]
    underTest.saveAll(releases)
  }

  void cleanup() {
    underTest.deleteAll()
  }

  @Unroll
  def "Find all after #date"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByReleaseDateAfter(date)

    then:
    results == expectedReleases

    where:
    date                       | expectedReleases
    LocalDate.of(2019, 12, 31) | [release1, release2, release3]
    LocalDate.of(2020, 1, 1)   | [release2, release3]
    LocalDate.of(2020, 2, 28)  | [release3]
    LocalDate.of(2020, 3, 31)  | []
  }

  @Unroll
  def "Find all between #from and #to"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByReleaseDateBetween(from, to)

    then:
    results == expectedReleases

    where:
    from                       | to                       | expectedReleases
    LocalDate.of(2019, 12, 31) | LocalDate.of(2020, 1, 1) | [release1]
    LocalDate.of(2020, 1, 1)   | LocalDate.of(2020, 2, 1) | [release1, release2]
    LocalDate.of(2020, 2, 1)   | LocalDate.of(2020, 1, 1) | []
  }

  @Unroll
  def "Find all after #date and artist in #artists"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByReleaseDateAfterAndArtistIn(date, artists)

    then:
    results == expectedReleases

    where:
    date                       | artists            | expectedReleases
    LocalDate.of(2019, 12, 31) | ["A1", "A2", "A3"] | [release1, release2, release3]
    LocalDate.of(2019, 12, 31) | ["A1", "A2"]       | [release1, release2]
    LocalDate.of(2019, 12, 31) | []                 | []
  }

  @Unroll
  def "Find all between #from and #to and artist in #artists"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByArtistInAndReleaseDateBetween(artists, from, to)

    then:
    results == expectedReleases

    where:
    from                       | to                       | artists            | expectedReleases
    LocalDate.of(2020, 1, 1)   | LocalDate.of(2020, 3, 1) | ["A1", "A2", "A3"] | [release1, release2, release3]
    LocalDate.of(2019, 12, 31) | LocalDate.of(2020, 1, 1) | ["A1"]             | [release1]
  }
}
