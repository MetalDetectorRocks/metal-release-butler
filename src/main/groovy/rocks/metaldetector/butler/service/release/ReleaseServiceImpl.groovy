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

  final Closure<PageRequest> pageableSupplier = { int page, int size, Sort sorting ->
    // Since the page is index-based we decrement the value by 1
    return PageRequest.of(page - 1, size, sorting)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, int page, int size, Sort sorting) {
    PageRequest pageRequest = pageableSupplier.call(page, size, sorting)
    def pageResult = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateAfterAndState(YESTERDAY, ReleaseEntityState.OK, pageRequest)
        : releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(YESTERDAY, artistNames, ReleaseEntityState.OK, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, int page, int size, Sort sorting) {
    PageRequest pageRequest = pageableSupplier.call(page, size, sorting)
    def pageResult = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateBetweenAndState(timeRange.from, timeRange.to, ReleaseEntityState.OK, pageRequest)
        : releaseRepository.findAllByArtistInAndReleaseDateBetweenAndState(artistNames, timeRange.from, timeRange.to, ReleaseEntityState.OK, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, int page, int size, Sort sorting) {
    PageRequest pageRequest = pageableSupplier.call(page, size, sorting)
    def pageResult = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateAfterAndState(dateFrom, ReleaseEntityState.OK, pageRequest)
        : releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(dateFrom, artistNames, ReleaseEntityState.OK, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, Sort sorting) {
    def releaseEntities = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateAfter(YESTERDAY, sorting)
        : releaseRepository.findAllByReleaseDateAfterAndArtistIn(YESTERDAY, artistNames, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, Sort sorting) {
    def releaseEntities = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateBetween(timeRange.from, timeRange.to, sorting)
        : releaseRepository.findAllByArtistInAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, Sort sorting) {
    def releaseEntities = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateAfter(dateFrom, sorting)
        : releaseRepository.findAllByReleaseDateAfterAndArtistIn(dateFrom, artistNames, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional
  void updateReleaseState(long releaseId, ReleaseEntityState state) {
    def releaseEntityOptional = releaseRepository.findById(releaseId)
    if (releaseEntityOptional.empty) {
      throw new IllegalArgumentException("${releaseId} not present!")
    }
    def releaseEntity = releaseEntityOptional.get()
    releaseEntity.state = state
    releaseRepository.save(releaseEntity)
  }

  @Override
  @Transactional
  void deleteByReleaseDetailsUrl(String releaseDetailsUrl) {
    releaseRepository.deleteByReleaseDetailsUrl(releaseDetailsUrl)
  }
}
