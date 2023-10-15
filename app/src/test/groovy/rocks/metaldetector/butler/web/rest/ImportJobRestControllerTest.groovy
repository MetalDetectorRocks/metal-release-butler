package rocks.metaldetector.butler.web.rest

import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.web.api.ImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobDto
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK

class ImportJobRestControllerTest extends Specification {

  ImportJobRestController underTest = new ImportJobRestController(importJobService: Mock(ImportJobService))

  def "Getting all import jobs should call ImportJobService"() {
    when:
    underTest.getAllImportJobs()

    then:
    1 * underTest.importJobService.findAllImportJobs()
  }

  def "Getting all import jobs should return wrapped result from ImportJobService"() {
    given:
    def importJobDto = new ImportJobDto(jobId: UUID.randomUUID())
    underTest.importJobService.findAllImportJobs() >> [importJobDto]

    when:
    def result = underTest.getAllImportJobs()

    then:
    ImportJobResponse importJobResponse = result.body
    importJobResponse.importJobs.size() == 1
    importJobResponse.importJobs[0] == importJobDto

    and:
    result.statusCode == OK
  }

  def "Getting all import jobs should return OK"() {
    when:
    def result = underTest.getAllImportJobs()

    then:
    result.statusCode == OK
  }

  def "Creating import job should call ImportJobService"() {
    when:
    underTest.createImportJob()

    then:
    1 * underTest.importJobService.createImportJobs()
  }

  def "Creating import job should return OK"() {
    when:
    def result = underTest.createImportJob()

    then:
    result.statusCode == OK
  }

  def "Creating import job should return jobIds"() {
    given:
    def expectedJobIds = ["1", "2", "3"]
    underTest.importJobService.createImportJobs() >> expectedJobIds

    when:
    def result = underTest.createImportJob()

    then:
    result.body.importJobIds == expectedJobIds
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

  def "Fetching an import job should call ImportJobService"() {
    given:
    def jobId = "someId"

    when:
    underTest.getImportJob(jobId)

    then:
    1 * underTest.importJobService.findImportJobById(jobId)
  }

  def "Fetching import job should return result from ImportJobService"() {
    given:
    def importJobDto = new ImportJobDto(jobId: UUID.randomUUID())
    underTest.importJobService.findImportJobById(*_) >> importJobDto

    when:
    def result = underTest.getImportJob("jobId")

    then:
    ImportJobDto importJob = result.body
    importJob == importJobDto

    and:
    result.statusCode == OK
  }

  def "Fetching import job should return result OK"() {
    when:
    def result = underTest.getImportJob("jobId")

    then:
    result.statusCode == OK
  }
}
