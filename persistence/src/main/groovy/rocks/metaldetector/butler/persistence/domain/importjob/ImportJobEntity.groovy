package rocks.metaldetector.butler.persistence.domain.importjob

import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder
import rocks.metaldetector.butler.persistence.domain.BaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import java.time.LocalDateTime

@Entity(name = "import_jobs")
@EqualsAndHashCode(callSuper = false)
@Builder(excludes = "new") // because there is method isNew in super class
class ImportJobEntity extends BaseEntity {

  @Column(name = "job_id", nullable = false)
  UUID jobId

  @Column(name = "total_count_requested", nullable = true)
  Integer totalCountRequested

  @Column(name = "total_count_imported", nullable = true)
  Integer totalCountImported

  @Column(name = "start_time", nullable = false, columnDefinition = "timestamp")
  LocalDateTime startTime

  @Column(name = "end_time", nullable = true, columnDefinition = "timestamp")
  LocalDateTime endTime

  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.STRING)
  JobState state

  @Column(name = "source", nullable = false)
  @Enumerated(EnumType.STRING)
  ReleaseSource source

}
