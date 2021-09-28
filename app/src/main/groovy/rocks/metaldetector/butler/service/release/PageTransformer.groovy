package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.web.api.Pagination

@Service
class PageTransformer {

  Pagination transform(Page<?> page) {
    return new Pagination(
            currentPage: page.number + 1,
            size: page.size,
            totalPages: page.totalPages,
            totalReleases: page.totalElements
    )
  }
}
