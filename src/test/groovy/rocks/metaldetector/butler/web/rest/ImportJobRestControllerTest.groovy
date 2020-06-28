package rocks.metaldetector.butler.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rocks.metaldetector.butler.service.importjob.ImportJobService
import rocks.metaldetector.butler.testutil.WithExceptionResolver
import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import spock.lang.Specification

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static rocks.metaldetector.butler.config.constants.Endpoints.IMPORT_JOB

class ImportJobRestControllerTest extends Specification implements WithExceptionResolver {

  ImportJobRestController underTest
  MockMvc mockMvc
  ObjectMapper objectMapper

  void setup() {
    underTest = new ImportJobRestController(importJobService: Mock(ImportJobService))
    mockMvc = MockMvcBuilders.standaloneSetup(underTest, exceptionResolver()).build()

    objectMapper = new ObjectMapper(dateFormat: new SimpleDateFormat("yyyy-MM-dd"))
    objectMapper.registerModule(new JavaTimeModule())
  }

  def "Creating import job should call release service"() {
    given:
    def request = post(IMPORT_JOB).accept(MediaType.APPLICATION_JSON)

    when:
    mockMvc.perform(request).andReturn()

    then:
    1 * underTest.importJobService.importFromExternalSources()
  }

  def "Creating import job should return result from release service"() {
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

  // ToDo DanielW: Test getAllImportJobResults
}
