package com.metalr2.butler.model.release

import org.springframework.data.jpa.repository.JpaRepository

import java.time.OffsetDateTime

interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  int deleteByReleaseDateIsAfter(OffsetDateTime offsetDateTime)

}