package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.model.release.ReleaseEntityState
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.web.api.ReleasesResponse

import java.time.LocalDate

@Service
@Slf4j
class ReleaseServiceImpl implements ReleaseService {

  static final YESTERDAY = LocalDate.now() - 1

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ReleasesResponseTransformer releasesResponseTransformer

  final Sort sorting = Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle")

  final Closure<PageRequest> pageableSupplier = { int page, int size ->
    // Since the page is index-based we decrement the value by 1
    return PageRequest.of(page - 1, size, sorting)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, ReleaseEntityState state, int page, int size) {
    PageRequest pageRequest = pageableSupplier.call(page, size)
    def pageResult = artistNames.isEmpty()
            ? releaseRepository.findAllByReleaseDateAfterAndState(YESTERDAY, state, pageRequest)
            : releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(YESTERDAY, artistNames, state, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, ReleaseEntityState state, int page, int size) {
    PageRequest pageRequest = pageableSupplier.call(page, size)
    def pageResult = artistNames.isEmpty()
            ? releaseRepository.findAllByReleaseDateBetweenAndState(timeRange.from, timeRange.to, state, pageRequest)
            : releaseRepository.findAllByArtistInAndReleaseDateBetweenAndState(artistNames, timeRange.from, timeRange.to, state, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, ReleaseEntityState state, int page, int size) {
    PageRequest pageRequest = pageableSupplier.call(page, size)
    def pageResult = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateAfterAndState(dateFrom, state, pageRequest)
        : releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(dateFrom, artistNames, state, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames) {
    def releaseEntities = artistNames.isEmpty()
            ? releaseRepository.findAllByReleaseDateAfter(YESTERDAY, sorting)
            : releaseRepository.findAllByReleaseDateAfterAndArtistIn(YESTERDAY, artistNames, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange) {
    def releaseEntities = artistNames.isEmpty()
            ? releaseRepository.findAllByReleaseDateBetween(timeRange.from, timeRange.to, sorting)
            : releaseRepository.findAllByArtistInAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom) {
    def releaseEntities = artistNames.isEmpty()
            ? releaseRepository.findAllByReleaseDateAfter(dateFrom, sorting)
            : releaseRepository.findAllByReleaseDateAfterAndArtistIn(dateFrom, artistNames, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional
  void updateReleaseState(long releaseId, ReleaseEntityState state) {
    def releaseEntityOptional = releaseRepository.findById(releaseId)
    if (releaseEntityOptional.isPresent()) {
      def releaseEntity = releaseEntityOptional.get()
      releaseEntity.state = state
      releaseRepository.save(releaseEntity)
    }
  }
}
