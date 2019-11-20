package com.metalr2.butler.service

import com.metalr2.butler.model.TimeRange
import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseRepository
import com.metalr2.butler.service.converter.Converter
import com.metalr2.butler.supplier.metalarchives.MetalArchivesRestClient
import com.metalr2.butler.web.dto.ReleaseDto
import com.metalr2.butler.web.dto.ReleaseImportResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate

@Service
class ReleaseServiceImpl implements ReleaseService {

  static final YESTERDAY = LocalDate.now() - 1

  final ReleaseRepository releaseRepository
  final MetalArchivesRestClient restClient
  final Converter<String[], List<ReleaseEntity>> converter

  final Closure<PageRequest> pageableSupplier = { int page, int size ->
    // Since the page is index-based we decrement the value by 1
    return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle"))
  }

  @Autowired
  ReleaseServiceImpl(ReleaseRepository releaseRepository,
                     MetalArchivesRestClient restClient,
                     Converter<String[], List<ReleaseEntity>> converter) {
    this.releaseRepository = releaseRepository
    this.restClient = restClient
    this.converter = converter
  }

  @Override
  @Transactional
  ReleaseImportResponse importFromExternalSource() {
    // query metal archives
    def upcomingReleasesRawData = restClient.requestReleases()

    // convert raw string data into ReleaseEntity
    List<ReleaseEntity> releaseEntities = upcomingReleasesRawData.collectMany { converter.convert(it) }

    // ToDo DanielW: only import releases that do not yet exist
//    releaseEntities.each { release ->
//      if (! releaseRepository.exists(release)) {
//        releaseRepository.save(release)
//      }
//    }

    // remove any record that has a release date today or later
    releaseRepository.deleteByReleaseDateAfter(YESTERDAY)

    // insert releases
    def inserted = releaseRepository.saveAll(releaseEntities.unique())

    return new ReleaseImportResponse(totalCountRequested: upcomingReleasesRawData.size(), totalCountImported: inserted.size())
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllUpcomingReleases(Iterable<String> artistNames, int page, int size) {
    PageRequest pageRequest = pageableSupplier(page, size)
    if (artistNames.isEmpty()) {
      return releaseRepository.findAllByReleaseDateAfter(YESTERDAY, pageRequest)
                              .collect { convertToDto(it) }
    }
    else {
      return releaseRepository.findAllByReleaseDateAfterAndArtistIn(YESTERDAY, artistNames, pageRequest)
                              .collect { convertToDto(it) }
    }
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, int page, int size) {
    PageRequest pageRequest = pageableSupplier(page, size)
    if (artistNames.isEmpty()) {
      return releaseRepository.findAllByReleaseDateBetween(timeRange.from, timeRange.to, pageRequest)
                              .collect { convertToDto(it) }
    }
    else {
      return releaseRepository.findAllByArtistInAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to, pageRequest)
                              .collect { convertToDto(it) }
    }
  }

  @Override
  @Transactional(readOnly = true)
  long totalCountAllUpcomingReleases(Iterable<String> artistNames) {
    if (artistNames.isEmpty()) {
      return releaseRepository.countByReleaseDateAfter(YESTERDAY)
    }
    else {
      return releaseRepository.countByArtistInAndReleaseDateAfter(artistNames, YESTERDAY)
    }
  }

  @Override
  @Transactional(readOnly = true)
  long totalCountAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange) {
    if (artistNames.isEmpty()) {
      return releaseRepository.countByReleaseDateBetween(timeRange.from, timeRange.to)
    }
    else {
      return releaseRepository.countByArtistInAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to)
    }
  }

  private ReleaseDto convertToDto(ReleaseEntity releaseEntity) {
    return new ReleaseDto(
            artist: releaseEntity.artist,
            additionalArtists: releaseEntity.additionalArtists,
            albumTitle: releaseEntity.albumTitle,
            releaseDate: releaseEntity.releaseDate,
            estimatedReleaseDate: releaseEntity.estimatedReleaseDate
    )
  }

}
