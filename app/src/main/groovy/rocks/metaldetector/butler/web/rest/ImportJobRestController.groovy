package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.web.api.ImportJobCreatedResponse
import rocks.metaldetector.butler.web.api.ImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobDto

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.FETCH_IMPORT_JOB
import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.IMPORT_JOB

@RestController
class ImportJobRestController {

  @Autowired
  ImportJobService importJobService

  @GetMapping(path = IMPORT_JOB, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<ImportJobResponse> getAllImportJobs() {
    List<ImportJobDto> importJobs = importJobService.findAllImportJobs()
    ImportJobResponse response = new ImportJobResponse(importJobs:  importJobs)
    return ResponseEntity.ok(response)
  }

  @GetMapping(path = FETCH_IMPORT_JOB, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<ImportJobDto> getImportJob(@PathVariable("jobId") String jobId) {
    return ResponseEntity.ok(importJobService.findImportJobById(jobId))
  }

  @PostMapping(path = IMPORT_JOB, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<ImportJobCreatedResponse> createImportJob() {
    List<String> jobIds = importJobService.createImportJobs()
    ImportJobCreatedResponse response = new ImportJobCreatedResponse(importJobIds: jobIds)
    return ResponseEntity.ok(response)
  }

  @PostMapping(path = COVER_JOB, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<Void> retryCoverDownload() {
    importJobService.retryCoverDownload()
    return ResponseEntity.ok().build()
  }
}
