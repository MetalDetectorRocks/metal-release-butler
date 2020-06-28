package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.testutil.WithExceptionResolver
import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobResponse
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

  def "Getting all import job results should return result from ImportJobServicee"() {
    given:
    def request = get(IMPORT_JOB).accept(MediaType.APPLICATION_JSON)
    def response = new ImportJobResponse(jobId: UUID.randomUUID())
    underTest.importJobService.findAllImportJobResults() >> [response]

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    ImportJobResponse[] importJobResponse = objectMapper.readValue(result.response.contentAsString, ImportJobResponse[])
    importJobResponse.size() == 1
    importJobResponse[0] == response

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

  def "Creating import job should return result from ImportJobService"() {
    given:
    def request = post(IMPORT_JOB).accept(MediaType.APPLICATION_JSON)
    def response = new CreateImportJobResponse(jobIds: [UUID.randomUUID()])
    underTest.importJobService.importFromExternalSources() >> response

    when:
    def result = mockMvc.perform(request).andReturn()

    then:
    CreateImportJobResponse importJobResponse = objectMapper.readValue(result.response.contentAsString, CreateImportJobResponse)
    importJobResponse == response

    and:
    result.response.status == OK.value()
  }
}
