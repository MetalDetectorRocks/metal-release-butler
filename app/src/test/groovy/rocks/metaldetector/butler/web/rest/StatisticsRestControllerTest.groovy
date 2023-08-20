package rocks.metaldetector.butler.web.rest

import rocks.metaldetector.butler.service.statistics.StatisticsService
import rocks.metaldetector.butler.web.api.ReleaseInfo
import rocks.metaldetector.butler.web.api.StatisticsResponse
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK

class StatisticsRestControllerTest extends Specification {

  StatisticsRestController underTest = new StatisticsRestController(statisticsService: Mock(StatisticsService))

  def "getting statistics should call statisticsService"() {
    when:
    underTest.getStatistics()

    then:
    1 * underTest.statisticsService.getReleaseInfo() >> new ReleaseInfo()
  }

  def "getting statistics should return statisticsResponse"() {
    given:
    def releaseInfo = new ReleaseInfo(upcomingReleases: 666)
    underTest.statisticsService.getReleaseInfo() >> releaseInfo

    when:
    def result = underTest.getStatistics()

    then:
    result.body == new StatisticsResponse(releaseInfo: releaseInfo)
  }

  def "getting statistics should return ok"() {
    when:
    def result = underTest.getStatistics()

    then:
    result.statusCode == OK
  }
}
