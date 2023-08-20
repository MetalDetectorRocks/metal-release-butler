package rocks.metaldetector.butler.web.api

import java.time.YearMonth

class ReleaseInfo {

  Map<YearMonth, Integer> releasesPerMonth
  long totalReleases
  int upcomingReleases
  int releasesThisMonth
  int duplicates
}
