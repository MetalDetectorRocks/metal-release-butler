package rocks.metaldetector.butler.persistence.domain.importjob

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import rocks.metaldetector.butler.persistence.WithIntegrationTestConfig
import rocks.metaldetector.butler.persistence.config.PersistenceConfig
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static rocks.metaldetector.butler.persistence.DtoFactory.ImportJobEntityFactory.createImportJobEntity
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.*
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TIME_FOR_METAL

@DataJpaTest
@ContextConfiguration(classes = [PersistenceConfig])
class ImportJobRepositoryIntegrationTest extends Specification implements WithIntegrationTestConfig {

  ImportJobEntity import1
  ImportJobEntity import2
  ImportJobEntity import3
  ImportJobEntity import4
  ImportJobEntity import5

  @Autowired
  ImportJobRepository underTest

  void setup() {
    import1 = createImportJobEntity(METAL_ARCHIVES, SUCCESSFUL, LocalDateTime.of(2020, 1, 1, 1, 1), LocalDateTime.of(2020, 1, 1, 1, 3))
    import2 = createImportJobEntity(METAL_ARCHIVES, SUCCESSFUL, LocalDateTime.of(2020, 1, 2, 1, 1), LocalDateTime.of(2020, 1, 2, 1, 3))
    import3 = createImportJobEntity(METAL_ARCHIVES, ERROR, LocalDateTime.of(2020, 1, 3, 1, 1), LocalDateTime.of(2020, 1, 3, 1, 3))
    import4 = createImportJobEntity(TIME_FOR_METAL, SUCCESSFUL, LocalDateTime.of(2020, 1, 1, 2, 1), LocalDateTime.of(2020, 1, 1, 2, 3))
    import5 = createImportJobEntity(TIME_FOR_METAL, RUNNING, LocalDateTime.of(2020, 1, 2, 2, 1), LocalDateTime.of(2020, 1, 2, 2, 3))
    def imports = [import1, import2, import3, import4, import5]
    underTest.saveAll(imports)
  }

  void cleanup() {
    underTest.deleteAll()
  }

  @Unroll
  "countBySource: should return correct number of imports for source '#source'"() {
    when:
    def result = underTest.countBySource(source)

    then:
    result == expectedResult

    where:
    source         | expectedResult
    METAL_ARCHIVES | 3
    TIME_FOR_METAL | 2
//    TEST           | 0
  }

  def "countBySourceAndState: should return correct number of imports for source '#source' and state '#state'"() {
    when:
    def result = underTest.countBySourceAndState(source, state)

    then:
    result == expectedResult

    where:
    source         | state      | expectedResult
    METAL_ARCHIVES | SUCCESSFUL | 2
    METAL_ARCHIVES | ERROR      | 1
    METAL_ARCHIVES | RUNNING    | 0
    TIME_FOR_METAL | SUCCESSFUL | 1
    TIME_FOR_METAL | ERROR      | 0
    TIME_FOR_METAL | RUNNING    | 1
  }

  def "findLastStartTime: should return correct time for source '#source'"() {
    when:
    def result = underTest.findLastStartTime(source)

    then:
    result == expectedResult

    where:
    source         | expectedResult
    METAL_ARCHIVES | LocalDateTime.of(2020, 1, 3, 1, 1)
    TIME_FOR_METAL | LocalDateTime.of(2020, 1, 2, 2, 1)
  }

  def "findLastStartTimeByState: should return correct time for source '#source' and state '#state'"() {
    when:
    def result = underTest.findLastStartTimeByState(source, state)

    then:
    result == expectedResult

    where:
    source         | state      | expectedResult
    METAL_ARCHIVES | SUCCESSFUL | LocalDateTime.of(2020, 1, 2, 1, 1)
    METAL_ARCHIVES | ERROR      | LocalDateTime.of(2020, 1, 3, 1, 1)
    METAL_ARCHIVES | RUNNING    | null
    TIME_FOR_METAL | SUCCESSFUL | LocalDateTime.of(2020, 1, 1, 2, 1)
    TIME_FOR_METAL | ERROR      | null
    TIME_FOR_METAL | RUNNING    | LocalDateTime.of(2020, 1, 2, 2, 1)
  }

  def "findByJobId: should return correct job"() {
    when:
    def result = underTest.findByJobId(import3.jobId)

    then:
    result.isPresent()

    and:
    result.get() == import3
  }
}
