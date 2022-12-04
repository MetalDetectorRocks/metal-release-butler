package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.web.api.ImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobDto

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.IMPORT_JOB

@RestController
class ImportJobRestController {

  @Autowired
  ImportJobService importJobService

  @GetMapping(path = IMPORT_JOB, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<ImportJobResponse> getAllImportJobResults() {
    List<ImportJobDto> importJobs = importJobService.findAllImportJobResults()
    ImportJobResponse response = new ImportJobResponse(importJobs:  importJobs)
    return ResponseEntity.ok(response)
  }

  @PostMapping(path = IMPORT_JOB, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<Void> createImportJob() {
    importJobService.importFromExternalSources()
    return ResponseEntity.ok().build()
  }

  @PostMapping(path = COVER_JOB, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<Void> retryCoverDownload() {
    importJobService.retryCoverDownload()
    return ResponseEntity.ok().build()
  }
}
