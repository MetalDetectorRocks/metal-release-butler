package rocks.metaldetector.butler.persistence.domain.importjob

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource

import java.time.LocalDateTime

@Repository
interface ImportJobRepository extends JpaRepository<ImportJobEntity, Long> {

  long countBySource(ReleaseSource source)

  long countBySourceAndState(ReleaseSource source, JobState state)

  @Query("select i.startTime from import_jobs i where i.source = :source order by i.startTime desc limit 1")
  LocalDateTime findLastStartTime(@Param("source") ReleaseSource source)

  @Query("select i.startTime from import_jobs i where i.source = :source and i.state = :state order by i.startTime desc limit 1")
  LocalDateTime findLastStartTimeByState(@Param("source") ReleaseSource source, @Param("state") JobState state)

  ImportJobEntity findByJobId(UUID jobId)
}
