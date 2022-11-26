package rocks.metaldetector.butler.web.rest

import groovy.util.logging.Slf4j
import jakarta.validation.ValidationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import rocks.metaldetector.butler.config.web.ResourceNotFoundException
import rocks.metaldetector.butler.web.api.ErrorResponse

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE

@ControllerAdvice
@Slf4j
class RestExceptionHandler {

  @ExceptionHandler([MissingServletRequestParameterException, HttpMessageNotReadableException])
  ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception exception, WebRequest webRequest) {
    log.warn("${webRequest.contextPath}: ${exception.message}")
    return ResponseEntity
        .badRequest()
        .body(new ErrorResponse(BAD_REQUEST.value(), BAD_REQUEST.reasonPhrase, exception.message))
  }

  @ExceptionHandler([HttpRequestMethodNotSupportedException])
  ResponseEntity<ErrorResponse> handleHttpMethodNotSupported(Exception exception, WebRequest webRequest) {
    log.warn("${webRequest.contextPath}: ${exception.message}")
    return ResponseEntity
        .status(METHOD_NOT_ALLOWED)
        .body(new ErrorResponse(METHOD_NOT_ALLOWED.value(), METHOD_NOT_ALLOWED.reasonPhrase, exception.message))
  }

  @ExceptionHandler([HttpMediaTypeNotSupportedException])
  ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(Exception exception, WebRequest webRequest) {
    log.warn("${webRequest.contextPath}: ${exception.message}")
    return ResponseEntity
        .status(UNSUPPORTED_MEDIA_TYPE)
        .body(new ErrorResponse(UNSUPPORTED_MEDIA_TYPE.value(), UNSUPPORTED_MEDIA_TYPE.reasonPhrase, exception.message))
  }

  @ExceptionHandler([ResourceNotFoundException])
  ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest webRequest) {
    log.warn("${webRequest.contextPath}: ${exception.message}")
    return ResponseEntity
        .status(NOT_FOUND)
        .body(new ErrorResponse(NOT_FOUND.value(), NOT_FOUND.reasonPhrase, exception.message))
  }

  @ExceptionHandler([ValidationException, MethodArgumentNotValidException])
  ResponseEntity<ErrorResponse> handleValidationError(Exception exception, WebRequest webRequest) {
    log.warn("${webRequest.contextPath}: ${exception.message}")
    return ResponseEntity
        .status(UNPROCESSABLE_ENTITY)
        .body(new ErrorResponse(UNPROCESSABLE_ENTITY.value(), UNPROCESSABLE_ENTITY.reasonPhrase, exception.message))
  }

  @ExceptionHandler([AccessDeniedException])
  ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception exception, WebRequest webRequest) {
    log.warn("${webRequest.contextPath}: ${exception.message}", exception)
    return ResponseEntity
        .status(FORBIDDEN)
        .body(new ErrorResponse(FORBIDDEN.value(), FORBIDDEN.reasonPhrase, exception.message))
  }

  @ExceptionHandler([Exception])
  ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception exception, WebRequest webRequest) {
    log.error("${webRequest.contextPath}: ${exception.message}", exception)
    return ResponseEntity
        .status(INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.reasonPhrase, exception.message))
  }
}
