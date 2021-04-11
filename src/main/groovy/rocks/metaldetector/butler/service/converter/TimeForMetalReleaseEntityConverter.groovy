package rocks.metaldetector.butler.service.converter

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rocks.metaldetector.butler.model.release.ReleaseEntity

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.model.release.ReleaseSource.TIME_FOR_METAL
import static rocks.metaldetector.butler.model.release.ReleaseType.EP
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH

@Component
@Slf4j
class TimeForMetalReleaseEntityConverter implements Converter<String, List<ReleaseEntity>> {

  static final String EP_SUFFIX = "(EP)"
  static final String ANY_WHITESPACE_REGEX = "\\s*"
  static final String ANY_DASH_REGEX = "\\p{Pd}"
  static final String ARTIST_ALBUM_NAME_DELIMITER_REGEX = "${ANY_WHITESPACE_REGEX}${ANY_DASH_REGEX}${ANY_WHITESPACE_REGEX}"

  @Autowired
  XmlSlurper xmlSlurper

  @Override
  List<ReleaseEntity> convert(String source) {
    def releaseTables = source?.findAll("(?s)<table class=\"events-table\".*?\\/table>")
    def releases = []

    releaseTables?.each {
      def releasesTable = xmlSlurper.parseText(it)
      releases += releasesTable.tbody.tr
          .collect {
            def builder = ReleaseEntity.builder()
                .source(TIME_FOR_METAL)
                .state(OK)
            setArtistName(builder, it.td[2].toString())
            setAlbumTitle(builder, it.td[2].toString())
            setReleaseDate(builder, it.td[0].toString())
            setReleaseType(builder, it.td[2].toString())
            def img = it.td[1].a[0].img[0]
            if (img instanceof NodeChild) {
              setReleaseDetailsUrl(builder, img as NodeChild)
            }
            builder.type(FULL_LENGTH)
            return builder.build()
          }
    }

    return releases as List<ReleaseEntity>
  }

  private void setArtistName(def builder, String rawValue) {
    String artistName = rawValue.split(ARTIST_ALBUM_NAME_DELIMITER_REGEX)[0]
    builder.artist(artistName.trim().strip())
  }

  private void setAlbumTitle(def builder, String rawValue) {
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

  private void setReleaseDetailsUrl(def builder, NodeChild rawSource) {
    def sourceUrl = rawSource.attributes()["src"] as String
    sourceUrl.replaceAll(ANY_DASH_REGEX, "-")
    builder.releaseDetailsUrl(sourceUrl)
  }
}
