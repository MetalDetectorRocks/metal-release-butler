package rocks.metaldetector.butler.persistence.domain.importjob

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImportJobRepository extends JpaRepository<ImportJobEntity, Long> {

}
