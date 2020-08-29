package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.web.api.ImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobDto

import static rocks.metaldetector.butler.config.constants.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.config.constants.Endpoints.IMPORT_JOB

@RestController
class ImportJobRestController {

  @Autowired
  ImportJobService importJobService

  @GetMapping(path = IMPORT_JOB, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  ResponseEntity<ImportJobResponse> getAllImportJobResults() {
    List<ImportJobDto> importJobs = importJobService.findAllImportJobResults()
    ImportJobResponse response = new ImportJobResponse(importJobs:  importJobs)
    return ResponseEntity.ok(response)
  }

  @PostMapping(path = IMPORT_JOB, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  ResponseEntity<Void> createImportJob() {
    importJobService.importFromExternalSources()
    return ResponseEntity.ok().build()
  }

  @PostMapping(path = COVER_JOB, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  ResponseEntity<Void> retryCoverDownload() {
    importJobService.retryCoverDownload()
    return ResponseEntity.ok().build()
  }
}
