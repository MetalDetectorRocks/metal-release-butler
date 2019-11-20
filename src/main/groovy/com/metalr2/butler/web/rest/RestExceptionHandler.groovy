package com.metalr2.butler.web.rest

import com.metalr2.butler.web.dto.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

import static org.springframework.http.HttpStatus.BAD_REQUEST

@ControllerAdvice
class RestExceptionHandler {

  final Logger log = LoggerFactory.getLogger(RestExceptionHandler)

  @ExceptionHandler(value = IllegalArgumentException)
  ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
    log.info(exception.message)
    return ResponseEntity.badRequest().body(new ErrorResponse(BAD_REQUEST.value(), BAD_REQUEST.reasonPhrase, exception.message))
  }

  @ExceptionHandler
  ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
    log.info(exception.message)
    def messages = exception.bindingResult.allErrors.collect { it.defaultMessage }.join(", ")
    return ResponseEntity.badRequest().body(new ErrorResponse(BAD_REQUEST.value(), "Constraint violation", messages))
  }

}
