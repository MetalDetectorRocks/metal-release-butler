package com.metalr2.butler.service

import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseRepository
import com.metalr2.butler.service.converter.Converter
import com.metalr2.butler.web.dto.ReleaseDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate

@Service
class ReleaseServiceImpl implements ReleaseService {

  static final YESTERDAY = LocalDate.now() - 1

  final Logger log = LoggerFactory.getLogger(ReleaseServiceImpl)
  final ReleaseRepository releaseRepository
  final Converter<String[], List<ReleaseEntity>> converter
  final Closure<PageRequest> pageableSupplier = { int page, int size ->
    // Since the page is index-based we decrement the value by 1
    return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle"))
  }

  @Autowired
  ReleaseServiceImpl(ReleaseRepository releaseRepository, Converter<String[], List<ReleaseEntity>> converter) {
    this.releaseRepository = releaseRepository
    this.converter = converter
  }

  @Override
  @Transactional
  void saveAll(List<String[]> upcomingReleasesRawData) {
    // convert raw string data into ReleaseEntity
    List<ReleaseEntity> releaseEntities = []
    upcomingReleasesRawData.each { releaseEntities.addAll(converter.convert(it)) }

    // remove any record that has a release date today or later
    def affectedRows = releaseRepository.deleteByReleaseDateAfter(YESTERDAY)
    log.info("{} records were deleted", affectedRows)

    // insert releases
    def inserted = releaseRepository.saveAll(releaseEntities.unique())
    log.info("{} records were inserted", inserted.size())
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllUpcomingReleases(int page, int size) {
    return releaseRepository.findAllByReleaseDateAfter(YESTERDAY, pageableSupplier(page, size)).collect { convertToDto(it) }
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllReleasesInTimeRange(LocalDate from, LocalDate to, int page, int size) {
    return releaseRepository.findAllByReleaseDateBetween(from, to, pageableSupplier(page, size)).collect { convertToDto(it) }
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllUpcomingReleasesForArtists(Iterable<String> artistNames, int page, int size) {
    return releaseRepository.findAllByArtistIn(artistNames, pageableSupplier(page, size)).collect { convertToDto(it) }
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllReleasesInTimeRangeForArtists(Iterable<String> artistNames, LocalDate from, LocalDate to, int page, int size) {
    return releaseRepository.findAllByArtistInAndReleaseDateBetween(artistNames, from, to, pageableSupplier(page, size)).collect { convertToDto(it) }
  }

  @Override
  long totalCountAllUpcomingReleases() {
    return releaseRepository.countByReleaseDateAfter(YESTERDAY)
  }

  @Override
  long totalCountAllReleasesInTimeRange(LocalDate from, LocalDate to) {
    return releaseRepository.countByReleaseDateBetween(from, to)
  }

  @Override
  long totalCountAllUpcomingReleasesForArtists(Iterable<String> artistNames) {
    return releaseRepository.countByArtistIn(artistNames)
  }

  @Override
  long totalCountAllReleasesInTimeRangeForArtists(Iterable<String> artistNames, LocalDate from, LocalDate to) {
    return releaseRepository.countByArtistInAndReleaseDateBetween(artistNames, from, to)
  }

  private ReleaseDto convertToDto(ReleaseEntity releaseEntity) {
    return new ReleaseDto(artist: releaseEntity.artist, additionalArtists: releaseEntity.additionalArtists,
                          albumTitle: releaseEntity.albumTitle, releaseDate: releaseEntity.releaseDate,
                          estimatedReleaseDate: releaseEntity.estimatedReleaseDate)
  }

}
