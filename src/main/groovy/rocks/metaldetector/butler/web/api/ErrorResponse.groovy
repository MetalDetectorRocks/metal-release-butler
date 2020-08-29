package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical

import java.time.Instant

@Canonical
class ErrorResponse {

  final Instant timestamp
  final int status
  final String error
  final String message

  ErrorResponse(int status, String error, String message) {
    this.timestamp = Instant.now()
    this.status = status
    this.error = error
    this.message = message
  }

}
