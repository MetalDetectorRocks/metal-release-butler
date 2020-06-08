package rocks.metaldetector.butler.model.importjob

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImportJobRepository extends JpaRepository<ImportJobEntity, Long> {

}