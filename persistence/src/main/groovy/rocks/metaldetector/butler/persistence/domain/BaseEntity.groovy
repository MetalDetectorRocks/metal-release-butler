package rocks.metaldetector.butler.persistence.domain

import groovy.transform.EqualsAndHashCode
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import java.time.LocalDateTime
import java.time.ZoneId

import static jakarta.persistence.GenerationType.IDENTITY
import static jakarta.persistence.TemporalType.TIMESTAMP

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
