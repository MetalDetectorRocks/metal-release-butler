package com.metalr2.butler.model.release

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

import java.time.LocalDate

interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  Page<ReleaseEntity> findAllByReleaseDateAfter(LocalDate date, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateBetween(LocalDate from, LocalDate to, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateAfterAndArtistIn(LocalDate date, Iterable<String> artistNames, Pageable pageable)

  Page<ReleaseEntity> findAllByArtistInAndReleaseDateBetween(Iterable<String> artistNames, LocalDate from, LocalDate to, Pageable pageable)

  long countByReleaseDateAfter(LocalDate date)

  long countByArtistInAndReleaseDateAfter(Iterable<String> artistNames, LocalDate date)

  long countByReleaseDateBetween(LocalDate from, LocalDate to)

  long countByArtistInAndReleaseDateBetween(Iterable<String> artistNames, LocalDate from, LocalDate to)

  int deleteByReleaseDateAfter(LocalDate date)

}