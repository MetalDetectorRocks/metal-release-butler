package com.metalr2.butler.web.dto

import groovy.transform.Canonical

import java.time.OffsetDateTime

@Canonical
class ErrorResponse {

  final OffsetDateTime timestamp
  final int httpStatus
  final List<String> messages

  ErrorResponse(List<String> messages, int httpStatus) {
    this.timestamp = OffsetDateTime.now()
    this.httpStatus = httpStatus
    this.messages  = messages
  }

}
