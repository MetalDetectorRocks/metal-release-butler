package com.metalr2.butler.web.rest

import com.metalr2.butler.web.dto.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class RestExceptionHandler {

  final Logger log = LoggerFactory.getLogger(RestExceptionHandler)

  @ExceptionHandler(value = TypeMismatchException)
  ResponseEntity<ErrorResponse> handleTypeMismatchException(TypeMismatchException exception) {
    log.warn(exception.getMessage())
    return ResponseEntity.badRequest().body(createTypeMismatchErrorResponse(exception))
  }

  @ExceptionHandler(value = IllegalArgumentException)
  ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
    log.warn(exception.getMessage())
    def messages = [exception.message]
    return ResponseEntity.badRequest().body(new ErrorResponse(messages, HttpStatus.BAD_REQUEST.value()))
  }

  @ExceptionHandler(value = Exception)
  ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception exception) {
    log.warn(exception.getMessage())
    def response = new ErrorResponse(["Unhandled server error"], HttpStatus.INTERNAL_SERVER_ERROR.value())
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
  }

  private ErrorResponse createTypeMismatchErrorResponse(TypeMismatchException exception) {
    def messages = []
    messages << "${exception.errorCode}: value '${exception.value}' cannot be converted to datatype '${exception.requiredType}'!".toString()
    messages << exception.message

    return new ErrorResponse(messages, HttpStatus.BAD_REQUEST.value())
  }

}
