package com.metalr2.butler.web.dto

import groovy.transform.Canonical

import java.time.Instant

@Canonical
class ErrorResponse {

  final Instant timestamp
  final int httpStatus
  final List<String> messages

  ErrorResponse(List<String> messages, int httpStatus) {
    this.timestamp = Instant.now()
    this.httpStatus = httpStatus
    this.messages  = messages
  }

}
