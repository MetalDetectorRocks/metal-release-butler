package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ImportResult
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ReleaseImporter
import spock.lang.Specification

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.ERROR
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.RUNNING
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.SUCCESSFUL

class ImportJobTaskTest extends Specification {

  ImportJobTask underTest = new ImportJobTask(releaseImporter: Mock(ReleaseImporter),
                                              importJobService: Mock(ImportJobService),
                                              importJob: Mock(ImportJobEntity))

  def "importJob's state is updated to RUNNING"() {
    when:
    underTest.run()

    then:
    1 * underTest.importJobService.updateImportJob(underTest.importJob, RUNNING)
  }

  def "releaseImporter is triggered"() {
    when:
    underTest.run()

    then:
    1 * underTest.releaseImporter.importReleases()
  }

  def "importJob's state is updated to SUCCESSFUL"() {
    given:
    def jobResult = new ImportResult(totalCountImported: 666)
    underTest.releaseImporter.importReleases() >> jobResult

    when:
    underTest.run()

    then:
    1 * underTest.importJobService.updateImportJob(underTest.importJob, SUCCESSFUL, jobResult)
  }

  def "importJob's state is updated to ERROR"() {
    given:
    underTest.releaseImporter.importReleases() >> { throw new RuntimeException("BOOM") }

    when:
    underTest.run()

    then:
    1 * underTest.importJobService.updateImportJob(underTest.importJob, ERROR)
  }
}
