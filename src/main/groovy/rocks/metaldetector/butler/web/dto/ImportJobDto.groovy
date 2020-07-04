package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical

import java.time.LocalDateTime

@Canonical
class ImportJobDto {

  UUID jobId
  int totalCountRequested
  int totalCountImported
  LocalDateTime startTime
  LocalDateTime endTime
  String source

}
