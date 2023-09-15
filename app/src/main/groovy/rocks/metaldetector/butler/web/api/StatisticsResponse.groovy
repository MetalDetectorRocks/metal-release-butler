package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical

@Canonical
class StatisticsResponse {

  ReleaseInfo releaseInfo
  List<ImportInfo> importInfo
}
