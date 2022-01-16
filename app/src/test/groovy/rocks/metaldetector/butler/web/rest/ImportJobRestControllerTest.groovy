package rocks.metaldetector.butler.web.rest

import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.web.api.ImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobDto
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK

class ImportJobRestControllerTest extends Specification {

  ImportJobRestController underTest = new ImportJobRestController(importJobService: Mock(ImportJobService))

  def "Getting all import job results should call ImportJobService"() {
    when:
    underTest.getAllImportJobResults()

    then:
    1 * underTest.importJobService.findAllImportJobResults()
  }

  def "Getting all import job results should return wrapped result from ImportJobServicee"() {
    given:
    def importJobDto = new ImportJobDto(jobId: UUID.randomUUID())
    underTest.importJobService.findAllImportJobResults() >> [importJobDto]

    when:
    def result = underTest.getAllImportJobResults()

    then:
    ImportJobResponse importJobResponse = result.body
    importJobResponse.importJobs.size() == 1
    importJobResponse.importJobs[0] == importJobDto

    and:
    result.statusCode == OK
  }

  def "Creating import job should call ImportJobService"() {
    when:
    underTest.createImportJob()

    then:
    1 * underTest.importJobService.importFromExternalSources()
  }

  def "Creating import job should return OK"() {
    when:
    def result = underTest.createImportJob()

    then:
    result.statusCode == OK
  }

  def "Retrying cover download should call ImportJobService"() {
    when:
    underTest.retryCoverDownload()

    then:
    1 * underTest.importJobService.retryCoverDownload()
  }

  def "Retrying cover download should return OK"() {
   when:
    def result = underTest.retryCoverDownload()

    then:
    result.statusCode == OK
  }
}
