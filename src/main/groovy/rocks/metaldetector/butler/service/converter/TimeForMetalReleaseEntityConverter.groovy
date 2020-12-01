package rocks.metaldetector.butler.service.converter

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseType

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.model.release.ReleaseSource.TIME_FOR_METAL
import static rocks.metaldetector.butler.model.release.ReleaseType.*

@Component
@Slf4j
class TimeForMetalReleaseEntityConverter implements Converter<String, List<ReleaseEntity>> {

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
            setCoverSourceUrl(builder, it.td[1].a[0].img[0] as NodeChild)
            builder.type(FULL_LENGTH)
            return builder.build()
          }
    }

    return releases as List<ReleaseEntity>
  }

  private void setArtistName(def builder, String string) {
    builder.artist(string.split(" - ")[0])
  }

  private void setAlbumTitle(def builder, String string) {
    def albumTitle = ""
    string.split(" - ").eachWithIndex { it, index ->
      if (index == 1) {
        albumTitle = it
      } else {
        albumTitle += " - ${it}"
      }
    }
    builder.albumTitle(albumTitle)
  }

  private void setReleaseDate(def builder, String dateString) {
    def dateParts = dateString.split("\\.")
    builder.releaseDate(LocalDate.of(dateParts[2].toInteger(), dateParts[1].toInteger(), dateParts[0].toInteger()))
  }

  private void setCoverSourceUrl(def builder, NodeChild rawSource) {
    def sourceUrl = rawSource.attributes()["src"] as String
    builder.releaseDetailsUrl(sourceUrl)
  }
}
