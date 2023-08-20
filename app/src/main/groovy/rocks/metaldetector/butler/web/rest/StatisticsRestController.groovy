package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.service.statistics.StatisticsService
import rocks.metaldetector.butler.web.api.ReleaseInfo
import rocks.metaldetector.butler.web.api.StatisticsResponse

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.STATISTICS

@RestController
class StatisticsRestController {

  @Autowired
  StatisticsService statisticsService

  @GetMapping(path = STATISTICS, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<StatisticsResponse> getStatistics() {
    ReleaseInfo releaseInfo = statisticsService.getReleaseInfo()
    StatisticsResponse response = new StatisticsResponse(releaseInfo: releaseInfo)
    return ResponseEntity.ok(response)
  }
}
