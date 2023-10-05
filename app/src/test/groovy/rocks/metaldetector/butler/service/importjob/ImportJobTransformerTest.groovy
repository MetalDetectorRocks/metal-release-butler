package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.persistence.domain.importjob.JobState
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class ImportJobTransformerTest extends Specification {

  ImportJobTransformer underTest = new ImportJobTransformer()

  def "should be null safe"() {
    expect:
    underTest.transform(null) == null
  }

  def "should transform 'jobId'"() {
    given:
    def jobId = UUID.randomUUID()
    def importJobEntity = new ImportJobEntity(jobId: jobId)

    when:
    def result = underTest.transform(importJobEntity)

    then:
    result.jobId == jobId.toString()
  }

  def "should transform 'totalCountRequested'"() {
    given:
    def totalCountRequested = 666
    def importJobEntity = new ImportJobEntity(totalCountRequested: totalCountRequested)

    when:
    def result = underTest.transform(importJobEntity)

    then:
    result.totalCountRequested == totalCountRequested
  }

  def "should transform 'totalCountImported'"() {
    given:
    def totalCountImported = 666
    def importJobEntity = new ImportJobEntity(totalCountImported: totalCountImported)

    when:
    def result = underTest.transform(importJobEntity)

    then:
    result.totalCountImported == totalCountImported
  }

  def "should transform 'startTime'"() {
    given:
    def startTime = LocalDateTime.now()
    def importJobEntity = new ImportJobEntity(startTime: startTime)

    when:
    def result = underTest.transform(importJobEntity)

    then:
    result.startTime == startTime
  }

  def "should transform 'endTime'"() {
    given:
    def endTime = LocalDateTime.now()
    def importJobEntity = new ImportJobEntity(endTime: endTime)

    when:
    def result = underTest.transform(importJobEntity)

    then:
    result.endTime == endTime
  }

  @Unroll
  "should transform 'state'"() {
    given:
    def importJobEntity = new ImportJobEntity(state: givenState)

    when:
    def result = underTest.transform(importJobEntity)

    then:
    result.state == ((JobState) givenState).displayName

    where:
    givenState << JobState.values()
  }

  @Unroll
  "should transform 'source'"() {
    given:
    def importJobEntity = new ImportJobEntity(source: givenSource)

    when:
    def result = underTest.transform(importJobEntity)

    then:
    result.source == ((ReleaseSource) givenSource).displayName

    where:
    givenSource << ReleaseSource.values()
  }
}
