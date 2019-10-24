package com.metalr2.butler.model.release

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

import java.time.OffsetDateTime

interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  Page<ReleaseEntity> findAllByReleaseDateIsAfter(OffsetDateTime offsetDateTime, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateIsBefore(OffsetDateTime offsetDateTime, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateIsBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable)

  long countByReleaseDateIsAfter(OffsetDateTime offsetDateTime)

  long countByReleaseDateIsBefore(OffsetDateTime offsetDateTime)

  long countByReleaseDateIsBetween(OffsetDateTime from, OffsetDateTime to)

  int deleteByReleaseDateIsAfter(OffsetDateTime offsetDateTime)

}