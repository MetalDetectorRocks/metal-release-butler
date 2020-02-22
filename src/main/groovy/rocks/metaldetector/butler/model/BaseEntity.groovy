package rocks.metaldetector.butler.model

import groovy.transform.EqualsAndHashCode
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Temporal
import java.time.LocalDateTime
import java.time.ZoneId

import static javax.persistence.GenerationType.IDENTITY
import static javax.persistence.TemporalType.TIMESTAMP

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(includes = "id")
abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  Long id

  @CreatedDate
  @Temporal(TIMESTAMP)
  @Column(name = "created_date", updatable = false)
  Date createdDateTime

  @LastModifiedDate
  @Temporal(TIMESTAMP)
  @Column(name = "last_modified_date")
  Date lastModifiedDateTime

  boolean isNew() {
    return id == null
  }

  LocalDateTime getCreatedDateTime() {
    return createdDateTime != null ? createdDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null
  }

  LocalDateTime getLastModifiedDateTime() {
    return lastModifiedDateTime != null ? lastModifiedDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null
  }

}
