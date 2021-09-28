package rocks.metaldetector.butler.service.release

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.persistence.domain.TimeRange
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.web.api.ReleasesResponse

import java.time.LocalDate

@Service
@Slf4j
class ReleaseServiceImpl implements ReleaseService {

  static final YESTERDAY = LocalDate.now() - 1
  static final TODAY = LocalDate.now()

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
  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, String query, int page, int size, Sort sorting) {
    PageRequest pageRequest = pageableSupplier.call(page, size, sorting)
    def pageResult = artistNames.isEmpty()
        ? releaseRepository.findAllReleasesFrom(TODAY, query, pageRequest)
        : releaseRepository.findAlleReleasesFromWithArtists(TODAY, artistNames, query, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, String query, int page, int size, Sort sorting) {
    PageRequest pageRequest = pageableSupplier.call(page, size, sorting)
    def pageResult = artistNames.isEmpty()
        ? releaseRepository.findAllReleasesBetween(timeRange.from, timeRange.to, query, pageRequest)
        : releaseRepository.findAllReleasesBetweenWithArtists(artistNames, timeRange.from, timeRange.to, query, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, String query, int page, int size, Sort sorting) {
    PageRequest pageRequest = pageableSupplier.call(page, size, sorting)
    def pageResult = artistNames.isEmpty()
        ? releaseRepository.findAllReleasesFrom(dateFrom, query, pageRequest)
        : releaseRepository.findAlleReleasesFromWithArtists(dateFrom, artistNames, query, pageRequest)

    return releasesResponseTransformer.transformPage(pageResult)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, Sort sorting) {
    def releaseEntities = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateAfter(YESTERDAY, sorting)
        : releaseRepository.findAllByReleaseDateAfterAndArtistInIgnoreCase(YESTERDAY, artistNames, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, Sort sorting) {
    def releaseEntities = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateBetween(timeRange.from, timeRange.to, sorting)
        : releaseRepository.findAllByArtistInIgnoreCaseAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to, sorting)

    return releasesResponseTransformer.transformReleaseEntities(releaseEntities)
  }

  @Override
  @Transactional(readOnly = true)
  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, Sort sorting) {
    def releaseEntities = artistNames.isEmpty()
        ? releaseRepository.findAllByReleaseDateAfter(dateFrom, sorting)
        : releaseRepository.findAllByReleaseDateAfterAndArtistInIgnoreCase(dateFrom, artistNames, sorting)

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
