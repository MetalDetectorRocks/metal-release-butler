package com.metalr2.butler.model.release

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

import java.time.LocalDate

interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  Page<ReleaseEntity> findAllByReleaseDateIsAfter(LocalDate date, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateIsBetween(LocalDate from, LocalDate to, Pageable pageable)

  long countByReleaseDateIsAfter(LocalDate date)

  long countByReleaseDateIsBetween(LocalDate from, LocalDate to)

  int deleteByReleaseDateIsAfter(LocalDate date)

}