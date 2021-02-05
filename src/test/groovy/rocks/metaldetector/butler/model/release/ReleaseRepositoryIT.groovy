package rocks.metaldetector.butler.model.release

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import rocks.metaldetector.butler.testutil.WithIntegrationTestConfig
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory

@DataJpaTest
class ReleaseRepositoryIT extends Specification implements WithIntegrationTestConfig {

  @Autowired
  ReleaseRepository underTest

  static String RELEASE_DETAILS_URL = "release-details-url"
  static String RELEASE_DETAILS_ALBUM_TITLE = "T"
  static ReleaseEntity release1 = ReleaseEntityFactory.createReleaseEntity(1L, "A1", LocalDate.of(2020, 1, 1))
  static ReleaseEntity release2 = ReleaseEntityFactory.createReleaseEntity(2L, "A2", LocalDate.of(2020, 2, 1))
  static ReleaseEntity release3 = ReleaseEntityFactory.createReleaseEntity(3L, "A3", LocalDate.of(2020, 3, 1))
  static Sort sorting = Sort.by(Sort.Direction.ASC, "releaseDate")

  void setup() {
    def releases = [release1, release2, release3]
    releases*.setReleaseDetailsUrl(RELEASE_DETAILS_URL)
    releases*.setAlbumTitle(RELEASE_DETAILS_ALBUM_TITLE)
    underTest.saveAll(releases)
  }

  void cleanup() {
    underTest.deleteAll()
  }

  def "findAllReleasesFrom: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)

    when:
    def result = underTest.findAllReleasesFrom(LocalDate.of(2020, 2, 1), null, pageRequest)

    then:
    result.content.size() == 2

    and:
    result.content == [release2, release3]
  }

  def "findAllReleasesFrom with query artistName: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "a2"

    when:
    def result = underTest.findAllReleasesFrom(LocalDate.of(2020, 2, 1), query, pageRequest)

    then:
    result.content.size() == 1

    and:
    result.content == [release2]
  }

  def "findAllReleasesFrom with query albumTitle: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "t"

    when:
    def result = underTest.findAllReleasesFrom(LocalDate.of(2020, 2, 1), query, pageRequest)

    then:
    result.content.size() == 2

    and:
    result.content == [release2, release3]
  }

  def "findAllReleasesBetween: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)

    when:
    def result = underTest.findAllReleasesBetween(from, to, null, pageRequest)

    then:
    result.content.size() == 2

    and:
    result.content == [release1, release2]
  }

  def "findAllReleasesBetween with query artistName: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "a2"
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)

    when:
    def result = underTest.findAllReleasesBetween(from, to, query, pageRequest)

    then:
    result.content.size() == 1

    and:
    result.content == [release2]
  }

  def "findAllReleasesBetween with query albumTitle: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "t"
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)

    when:
    def result = underTest.findAllReleasesBetween(from, to, query, pageRequest)

    then:
    result.content.size() == 2

    and:
    result.content == [release1, release2]
  }

  def "findAlleReleasesFromWithArtists: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def artists = ["a1"]

    when:
    def result = underTest.findAlleReleasesFromWithArtists(LocalDate.of(2020, 1, 1), artists, null, pageRequest)

    then:
    result.content.size() == 1

    and:
    result.content == [release1]
  }

  def "findAlleReleasesFromWithArtists with query artistName: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "a2"
    def artists = ["a1", "a2"]

    when:
    def result = underTest.findAlleReleasesFromWithArtists(LocalDate.of(2020, 1, 1), artists, query, pageRequest)

    then:
    result.content.size() == 1

    and:
    result.content == [release2]
  }

  def "findAlleReleasesFromWithArtists with query albumTitle: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "t"
    def artists = ["a1", "a2"]

    when:
    def result = underTest.findAlleReleasesFromWithArtists(LocalDate.of(2020, 1, 1), artists, query, pageRequest)

    then:
    result.content.size() == 2

    and:
    result.content == [release1, release2]
  }

  def "findAllReleasesBetweenWithArtists: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = ["a1"]

    when:
    def result = underTest.findAllReleasesBetweenWithArtists(artists, from, to, null, pageRequest)

    then:
    result.content.size() == 1

    and:
    result.content == [release1]
  }

  def "findAllReleasesBetweenWithArtists with query artistName: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "a2"
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = ["a1", "a2"]

    when:
    def result = underTest.findAllReleasesBetweenWithArtists(artists, from, to, query, pageRequest)

    then:
    result.content.size() == 1

    and:
    result.content == [release2]
  }

  def "findAllReleasesBetweenWithArtists with query albumTitle: should return correct releases"() {
    given:
    def pageRequest = PageRequest.of(0, 2)
    def query = "t"
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def artists = ["a1", "a2"]

    when:
    def result = underTest.findAllReleasesBetweenWithArtists(artists, from, to, query, pageRequest)

    then:
    result.content.size() == 2

    and:
    result.content == [release1, release2]
  }

  @Unroll
  "Find all after #date"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByReleaseDateAfter(date, sorting)

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
  "Find all between #from and #to"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByReleaseDateBetween(from, to, sorting)

    then:
    results == expectedReleases

    where:
    from                       | to                       | expectedReleases
    LocalDate.of(2019, 12, 31) | LocalDate.of(2020, 1, 1) | [release1]
    LocalDate.of(2020, 1, 1)   | LocalDate.of(2020, 2, 1) | [release1, release2]
    LocalDate.of(2020, 2, 1)   | LocalDate.of(2020, 1, 1) | []
  }

  @Unroll
  "Find all after #date and artist in #artists"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByReleaseDateAfterAndArtistInIgnoreCase(date, artists, sorting)

    then:
    results == expectedReleases

    where:
    date                       | artists            | expectedReleases
    LocalDate.of(2019, 12, 31) | ["A1", "A2", "A3"] | [release1, release2, release3]
    LocalDate.of(2019, 12, 31) | ["A1", "A2"]       | [release1, release2]
    LocalDate.of(2019, 12, 31) | []                 | []
  }

  @Unroll
  "Find all between #from and #to and artist in #artists"() {
    when:
    List<ReleaseEntity> results = underTest.findAllByArtistInIgnoreCaseAndReleaseDateBetween(artists, from, to, sorting)

    then:
    results == expectedReleases

    where:
    from                       | to                       | artists            | expectedReleases
    LocalDate.of(2020, 1, 1)   | LocalDate.of(2020, 3, 1) | ["A1", "A2", "A3"] | [release1, release2, release3]
    LocalDate.of(2019, 12, 31) | LocalDate.of(2020, 1, 1) | ["A1"]             | [release1]
  }

  @Unroll
  "Release already exists"() {
    when:
    boolean result = underTest.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(artist, albumTitle, releaseDate)

    then:
    result == expectedResult

    where:
    artist                        | albumTitle                        | releaseDate          | expectedResult
    null                          | null                              | null                 | false
    release1.artist               | release1.albumTitle               | release1.releaseDate | true
    release1.artist.toLowerCase() | release1.albumTitle               | release1.releaseDate | true
    release1.artist               | release1.albumTitle.toLowerCase() | release1.releaseDate | true
    release2.artist               | release1.albumTitle               | release1.releaseDate | false
  }

  def "should delete entities by release details url"() {
    when:
    underTest.deleteByReleaseDetailsUrl(RELEASE_DETAILS_URL)

    then:
    underTest.findAll().isEmpty()
  }
}
