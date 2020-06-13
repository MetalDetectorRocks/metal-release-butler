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

  final XmlSlurper xmlSlurper

  MetalHammerReleaseEntityConverter() {
    this.xmlSlurper = new XmlSlurper()
    this.xmlSlurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
  }

  @Override
  List<ReleaseEntity> convert(String source) {
    source = source.find("(?s)<table.*\\/table>")
    def releasesPage = xmlSlurper.parseText(source)
    def releases = releasesPage.tbody.tr
        .findAll { it != releasesPage.tbody.tr.first() }
        .collect {
          def builder = ReleaseEntity.builder()
              .artist(it.td[0].toString())
              .albumTitle(WordUtils.capitalizeFully(it.td[1].toString()))
              .source(METAL_HAMMER_DE)
              .state(NOT_SET)
          setDate(builder, it.td[2].toString())
          return builder.build()
        }
    return releases
  }

  private void setDate(def builder, String dateString) {
    if (dateString.contains(".")) {
      def dayAndMonth = dateString.split("\\.")
      builder.releaseDate(LocalDate.of(LocalDate.now().getYear(), dayAndMonth[1] as int, dayAndMonth[0] as int))
    }
    else if (dateString.matches("^[0-9]+\$")) {
      builder.releaseDate(LocalDate.of(LocalDate.now().getYear(), 12, 31))
    }
    else if (dateString.matches("^[A-zÜü]+\$")) {
      builder.estimatedReleaseDate(dateString)
    }
  }
}
