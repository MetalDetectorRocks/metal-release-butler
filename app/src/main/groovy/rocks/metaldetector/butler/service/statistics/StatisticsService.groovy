package rocks.metaldetector.butler.service.statistics

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobRepository
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource
import rocks.metaldetector.butler.web.api.ImportInfo
import rocks.metaldetector.butler.web.api.ReleaseInfo

import java.time.LocalDate
import java.time.YearMonth

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.SUCCESSFUL
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.DEMO
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.DUPLICATE
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TEST

@Service
class StatisticsService {

  @Autowired
  ReleaseRepository releaseRepository
  @Autowired
  ImportJobRepository importJobRepository

  @Transactional(readOnly = true)
  ReleaseInfo getReleaseInfo() {
    def releasesPerMonth = releaseRepository.groupReleasesByYearAndMonth()
        .collectEntries { new MapEntry(YearMonth.of(it.releaseYear, it.releaseMonth), it.releases) } as TreeMap<YearMonth, Integer>
    def totalReleases = (releasesPerMonth.values().sum() ?: 0) as long
    def upcomingReleases = releaseRepository.countByReleaseDateAfterAndStateNot(LocalDate.now(), DEMO)
    def duplicates = releaseRepository.countByState(DUPLICATE)

    return new ReleaseInfo(releasesPerMonth: releasesPerMonth,
                           totalReleases: totalReleases,
                           upcomingReleases: upcomingReleases,
                           releasesThisMonth: releasesPerMonth[YearMonth.now()] ?: 0,
                           duplicates: duplicates)
  }

  @Transactional(readOnly = true)
  List<ImportInfo> getImportInfo() {
    return (ReleaseSource.values() - TEST).collect { source ->
      long allImports = importJobRepository.countBySource(source)
      long successFullImports = importJobRepository.countBySourceAndState(source, SUCCESSFUL)
      int successRate = (successFullImports / allImports * 100).intValue()
      def lastImport = importJobRepository.findLastStartTime(source)
      def lastSuccessfulImport = importJobRepository.findLastStartTimeByState(source, SUCCESSFUL)
      return new ImportInfo(source: source.name(),
                            successRate: successRate,
                            lastImport: lastImport,
                            lastSuccessfulImport: lastSuccessfulImport)
    }
  }
}
