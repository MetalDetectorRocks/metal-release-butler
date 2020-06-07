package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.web.dto.ReleaseDto
import rocks.metaldetector.butler.web.dto.ReleaseImportResponse

import java.time.LocalDate

@Service
@Slf4j
class ReleaseServiceImpl implements ReleaseService {

  static final YESTERDAY = LocalDate.now() - 1

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ReleaseImportService metalArchivesReleaseImportService

  final Closure<PageRequest> pageableSupplier = { int page, int size ->
    // Since the page is index-based we decrement the value by 1
    return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle"))
  }

  @Override
  @Transactional
  ReleaseImportResponse importFromExternalSources() {
    return metalArchivesReleaseImportService.importReleases()
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllUpcomingReleases(Iterable<String> artistNames, int page, int size) {
    PageRequest pageRequest = pageableSupplier.call(page, size)
    if (artistNames.isEmpty()) {
      return releaseRepository
              .findAllByReleaseDateAfter(YESTERDAY, pageRequest)
              .collect { convertToDto(it) }
    }
    else {
      return releaseRepository
              .findAllByReleaseDateAfterAndArtistIn(YESTERDAY, artistNames, pageRequest)
              .collect { convertToDto(it) }
    }
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, int page, int size) {
    PageRequest pageRequest = pageableSupplier.call(page, size)
    if (artistNames.isEmpty()) {
      return releaseRepository
              .findAllByReleaseDateBetween(timeRange.from, timeRange.to, pageRequest)
              .collect { convertToDto(it) }
    }
    else {
      return releaseRepository
              .findAllByArtistInAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to, pageRequest)
              .collect { convertToDto(it) }
    }
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllUpcomingReleases(Iterable<String> artistNames) {
    if (artistNames.isEmpty()) {
      return releaseRepository
              .findAllByReleaseDateAfter(YESTERDAY)
              .collect { convertToDto(it) }
    }
    else {
      return releaseRepository
              .findAllByReleaseDateAfterAndArtistIn(YESTERDAY, artistNames)
              .collect { convertToDto(it) }
    }
  }

  @Override
  @Transactional(readOnly = true)
  List<ReleaseDto> findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange) {
    if (artistNames.isEmpty()) {
      return releaseRepository
              .findAllByReleaseDateBetween(timeRange.from, timeRange.to)
              .collect { convertToDto(it) }
    }
    else {
      return releaseRepository
              .findAllByArtistInAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to)
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
        estimatedReleaseDate: releaseEntity.estimatedReleaseDate,
        genre: releaseEntity.genre,
        type: releaseEntity.type,
        metalArchivesAlbumUrl: releaseEntity.metalArchivesAlbumUrl,
        metalArchivesArtistUrl: releaseEntity.metalArchivesArtistUrl,
        source: releaseEntity.source,
        state: releaseEntity.state
    )
  }
}
