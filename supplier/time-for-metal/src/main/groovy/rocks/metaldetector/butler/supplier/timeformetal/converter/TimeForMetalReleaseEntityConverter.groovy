package rocks.metaldetector.butler.supplier.timeformetal.converter

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.supplier.infrastructure.converter.Converter

import java.time.LocalDate

import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TIME_FOR_METAL
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseType.EP
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseType.FULL_LENGTH

@Component
@Slf4j
class TimeForMetalReleaseEntityConverter implements Converter<String, List<ReleaseEntity>> {

  static final char NBSP_HTML_ENTITY = '\u00A0'
  static final String ZWSP_HTML_ENTITY_REGEX = "[\u200B]*"
  static final String EP_SUFFIX = "(EP)"
  static final String ANY_WHITESPACE_REGEX = "\\s+"
  static final String ANY_DASH_REGEX = "\\p{Pd}"
  static final String ARTIST_ALBUM_NAME_DELIMITER_REGEX = "${ANY_WHITESPACE_REGEX}${ANY_DASH_REGEX}${ANY_WHITESPACE_REGEX}"

  @Autowired
  XmlSlurper xmlSlurper

  @Override
  List<ReleaseEntity> convert(String source) {
    List<String> releaseTables = source?.findAll("(?s)<table>.*?\\</table>")
    List<ReleaseEntity> releases = []

    releaseTables?.each {table ->
      def releasesTable = xmlSlurper.parseText(replaceTableTag(table))
      releasesTable.tr.eachWithIndex { it, index ->
        if (index != 0) { // skip table header
          def builder = ReleaseEntity.builder()
                  .source(TIME_FOR_METAL)
                  .state(OK)
                  .type(FULL_LENGTH)
          setArtistName(builder, it.th[2].h3.toString())
          setAlbumTitle(builder, it.th[2].h3.toString())
          setReleaseDate(builder, it.th[0].toString())
          setReleaseType(builder, it.th[2].h3.toString())
          setGenre(builder, it.th[2] as NodeChild)
          def img = it.th[1].a[0].img[0]
          if (img instanceof NodeChild) {
            setReleaseDetailsUrl(builder, img as NodeChild)
          }
          releases << builder.build()
        }
      }
    }

    return releases
  }

  // Hint: It doesn't work with pure <table>. I don't know what is the problem.
  private String replaceTableTag(String source) {
    return source
            .replace("<table>", "<releases>")
            .replace("</table>", "</releases>")
  }

  private void setArtistName(def builder, String rawValue) {
    rawValue = rawValue.replace(NBSP_HTML_ENTITY, (char) ' ').replaceAll(ZWSP_HTML_ENTITY_REGEX, "")
    String artistName = rawValue.split(ARTIST_ALBUM_NAME_DELIMITER_REGEX)[0]
    builder.artist(artistName.trim().strip())
  }

  private void setAlbumTitle(def builder, String rawValue) {
    rawValue = rawValue.replace(NBSP_HTML_ENTITY, (char) ' ').replaceAll(ZWSP_HTML_ENTITY_REGEX, "")
    def albumTitle = ""
    rawValue.split(ARTIST_ALBUM_NAME_DELIMITER_REGEX).eachWithIndex { it, index ->
      if (index == 1) {
        albumTitle = it
      } else {
        albumTitle += " - ${it}"
      }
    }
    albumTitle = albumTitle.replace(EP_SUFFIX, "")
    builder.albumTitle(albumTitle.trim().strip())
  }

  private void setReleaseDate(def builder, String dateString) {
    def dateParts = dateString.split("\\.")
    builder.releaseDate(LocalDate.of(dateParts[2].toInteger(), dateParts[1].toInteger(), dateParts[0].toInteger()))
  }

  private void setReleaseType(def builder, String rawValue) {
    rawValue.endsWith(EP_SUFFIX) ? builder.type(EP) : builder.type(FULL_LENGTH)
  }

  private void setReleaseDetailsUrl(def builder, NodeChild nodeChild) {
    def sourceUrl = nodeChild.attributes()["src"] as String
    sourceUrl.replaceAll(ANY_DASH_REGEX, "-")
    builder.releaseDetailsUrl(sourceUrl)
  }

  private void setGenre(def builder, NodeChild nodeChild) {
    def directStringNodes = nodeChild.localText()
    if (directStringNodes) {
      def genre = directStringNodes[0]
              .trim()
              .strip()
              .replace(NBSP_HTML_ENTITY, (char) ' ')
              .replaceAll(ZWSP_HTML_ENTITY_REGEX, "")
      builder.genre(genre)
    }
  }
}
