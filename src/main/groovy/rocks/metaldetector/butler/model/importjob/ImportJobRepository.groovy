package rocks.metaldetector.butler.model.importjob

import org.springframework.data.jpa.repository.JpaRepository

interface ImportJobRepository extends JpaRepository<ImportJobEntity, Long> {

}