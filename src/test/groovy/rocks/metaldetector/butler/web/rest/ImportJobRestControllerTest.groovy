package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.testutil.WithExceptionResolver
import rocks.metaldetector.butler.web.api.ImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobDto
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.config.constants.Endpoints.COVER_JOB
import static rocks.metaldetector.butler.config.constants.Endpoints.IMPORT_JOB

class ImportJobRestControllerTest extends Specification implements WithExceptionResolver {

  ImportJobRestController underTest = new ImportJobRestController(importJobService: Mock(ImportJobService))
  MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest, exceptionResolver()).build()
  ObjectMapper objectMapper = new ObjectMapper()

  def "Getting all import job results should call ImportJobService"() {
    given:
    def request = get(IMPORT_JOB).accept(MediaType.APPLICATION_JSON)

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.importJobService.findAllImportJobResults()
  }

  def "Getting all import job results should return wrapped result from ImportJobServicee"() {
    given:
    def request = get(IMPORT_JOB).accept(MediaType.APPLICATION_JSON)
    def importJobDto = new ImportJobDto(jobId: UUID.randomUUID())
    underTest.importJobService.findAllImportJobResults() >> [importJobDto]

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ImportJobResponse importJobResponse = objectMapper.readValue(result.response.contentAsString, ImportJobResponse)
    importJobResponse.importJobs.size() == 1
    importJobResponse.importJobs[0] == importJobDto

    and:
    result.response.status == OK.value()
  }

  def "Creating import job should call ImportJobService"() {
    given:
    def request = post(IMPORT_JOB).accept(MediaType.APPLICATION_JSON)

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.importJobService.importFromExternalSources()
  }

  def "Creating import job should return OK"() {
    given:
    def request = post(IMPORT_JOB).accept(MediaType.APPLICATION_JSON)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }

  def "Retrying cover download should call ImportJobService"() {
    given:
    def request = post(COVER_JOB).accept(MediaType.APPLICATION_JSON)

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.importJobService.retryCoverDownload()
  }

  def "Retrying cover download should return OK"() {
    given:
    def request = post(COVER_JOB).accept(MediaType.APPLICATION_JSON)

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    result.response.status == OK.value()
  }
}
