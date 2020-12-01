package rocks.metaldetector.butler.service.converter

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import org.apache.commons.lang.WordUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rocks.metaldetector.butler.model.release.ReleaseEntity

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH

@Component
@Slf4j
class MetalHammerReleaseEntityConverter implements Converter<String, List<ReleaseEntity>> {

  private static final String DATE_REGEX = "\\."
  private static final String YEAR_REGEX = "^[0-9]+\$"
  private static final String ESTIMATED_DATE_REGEX = "^[A-zÜü]+\$"
  static final String SPRING_GERMAN = "Frühling"
  static final String SUMMER_GERMAN = "Sommer"
  static final String AUTUMN_GERMAN = "Herbst"
  static final String WINTER_GERMAN = "Winter"
  static final String SPRING_ENGLISH = "Spring"
  static final String SUMMER_ENGLISH = "Summer"
  static final String AUTUMN_ENGLISH = "Autumn"
  static final String WINTER_ENGLISH = "Winter"

  @Autowired
  XmlSlurper xmlSlurper

  @Override
  List<ReleaseEntity> convert(String source) {
    source = source.find("(?s)<table.*\\/table>")
    def releasesPage = xmlSlurper.parseText(source)
    def releases = releasesPage.tbody.tr
        .findAll { it != releasesPage.tbody.tr.first() }
        .collect {
          def builder = ReleaseEntity.builder()
              .source(METAL_HAMMER_DE)
              .state(OK)
          setArtist(builder, it.td[0].toString())
          setAlbumTitle(builder, it.td[1].toString())
          setDate(builder, it.td[2].toString())
          builder.type(FULL_LENGTH)
          return builder.build()
        }
    return releases
  }

  private void setArtist(def builder, String artistString) {
    if (artistString.contains(", ")) {
      def nameParts = artistString.split(", ")
      def first = nameParts[1]
      def last = nameParts[0]
      builder.artist("$first $last")
    }
    else {
      builder.artist(artistString)
    }
  }

  private void setAlbumTitle(def builder, String albumTitle) {
    builder.albumTitle(WordUtils.capitalizeFully(albumTitle))
  }

  private void setDate(def builder, String dateString) {
    if (dateString.contains(".")) {
      def dayAndMonth = dateString.split(DATE_REGEX)
      if (dayAndMonth.size() == 2) {
        builder.releaseDate(LocalDate.of(LocalDate.now().getYear(), dayAndMonth[1] as int, dayAndMonth[0] as int))
      }
      else if (dayAndMonth.size() == 3) {
        builder.releaseDate(LocalDate.of(dayAndMonth[2] as int, dayAndMonth[1] as int, dayAndMonth[0] as int))
      }
    }
    else if (dateString.matches(YEAR_REGEX)) {
      builder.estimatedReleaseDate(dateString)
    }
    else if (dateString.matches(ESTIMATED_DATE_REGEX)) {
      setEstimatedReleaseDate(builder, dateString)
    }
  }

  private void setEstimatedReleaseDate(def builder, String dateString) {
    switch (dateString) {
      case SPRING_GERMAN: builder.estimatedReleaseDate(SPRING_ENGLISH)
        break
      case SUMMER_GERMAN: builder.estimatedReleaseDate(SUMMER_ENGLISH)
        break
      case AUTUMN_GERMAN: builder.estimatedReleaseDate(AUTUMN_ENGLISH)
        break
      case WINTER_GERMAN: builder.estimatedReleaseDate(WINTER_ENGLISH)
        break
    }
  }
}
