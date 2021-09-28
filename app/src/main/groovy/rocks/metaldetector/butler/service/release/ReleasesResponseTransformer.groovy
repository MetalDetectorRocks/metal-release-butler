package rocks.metaldetector.butler.service.release

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.web.api.Pagination
import rocks.metaldetector.butler.web.api.ReleasesResponse

@Service
class ReleasesResponseTransformer {

  @Autowired
  PageTransformer pageTransformer

  @Autowired
  ReleaseTransformer releaseTransformer

  ReleasesResponse transformPage(Page<ReleaseEntity> releaseEntityPage) {
    def pagination = pageTransformer.transform(releaseEntityPage)
    def releases = releaseEntityPage.collect { releaseTransformer.transform(it) }
    return new ReleasesResponse(pagination: pagination, releases: releases)
  }

  ReleasesResponse transformReleaseEntities(List<ReleaseEntity> releaseEntities) {
    def pagination = new Pagination(
            currentPage: 1,
            size: releaseEntities.size(),
            totalPages: 1,
            totalReleases: releaseEntities.size()
    )
    def releases = releaseEntities.collect { releaseTransformer.transform(it) }
    return new ReleasesResponse(pagination: pagination, releases: releases)
  }
}
