package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical

import java.time.LocalDateTime

@Canonical
class ImportJobDto {

  String jobId
  Integer totalCountRequested
  Integer totalCountImported
  LocalDateTime startTime
  LocalDateTime endTime
  String state
  String source

}
