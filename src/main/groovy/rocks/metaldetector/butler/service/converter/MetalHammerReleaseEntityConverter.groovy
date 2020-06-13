package rocks.metaldetector.butler.service.converter

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import org.apache.commons.lang.WordUtils
import org.springframework.stereotype.Component
import rocks.metaldetector.butler.model.release.ReleaseEntity

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseEntityRecordState.NOT_SET
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

@Component
@Slf4j
class MetalHammerReleaseEntityConverter implements Converter<String, List<ReleaseEntity>> {

  private static final String DATE_REGEX = "\\."
  private static final String YEAR_REGEX = "^[0-9]+\$"
  private static final String ESTIMATED_DATE_REGEX = "^[A-zÜü]+\$"

  final XmlSlurper xmlSlurper

  MetalHammerReleaseEntityConverter() {
    this.xmlSlurper = new XmlSlurper()
  }

  @Override
  List<ReleaseEntity> convert(String source) {
    source = source.find("(?s)<table.*\\/table>")
    def releasesPage = xmlSlurper.parseText(source)
    def releases = releasesPage.tbody.tr
        .findAll { it != releasesPage.tbody.tr.first() }
        .collect {
          def builder = ReleaseEntity.builder()
              .source(METAL_HAMMER_DE)
              .state(NOT_SET)
          setArtist(builder, it.td[0].toString())
          setAlbumTitle(builder, it.td[1].toString())
          setDate(builder, it.td[2].toString())
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
    } else {
      builder.artist(artistString)
    }
  }

  private void setAlbumTitle(def builder, String albumTitle) {
    builder.albumTitle(WordUtils.capitalizeFully(albumTitle))
  }

  private void setDate(def builder, String dateString) {
    if (dateString.contains(".")) {
      def dayAndMonth = dateString.split(DATE_REGEX)
      builder.releaseDate(LocalDate.of(LocalDate.now().getYear(), dayAndMonth[1] as int, dayAndMonth[0] as int))
    }
    else if (dateString.matches(YEAR_REGEX) || dateString.matches(ESTIMATED_DATE_REGEX)) {
      builder.estimatedReleaseDate(dateString)
    }
  }
}
