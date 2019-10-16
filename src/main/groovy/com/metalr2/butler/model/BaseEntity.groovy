package com.metalr2.butler.model

import groovy.transform.EqualsAndHashCode
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.*
import java.time.LocalDateTime
import java.time.ZoneId

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(includes = "id")
abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_date", updatable = false)
  Date createdDateTime

  @LastModifiedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_date")
  Date lastModifiedDateTime

  // ToDo DanielW: currently we have an exception on startup due to this method. this needs investigation
//  boolean isNew() {
//    return id == null
//  }

  LocalDateTime getCreatedDateTime() {
    return createdDateTime != null ? createdDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null
  }

  LocalDateTime getLastModifiedDateTime() {
    return lastModifiedDateTime != null ? lastModifiedDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null
  }

}
