package com.prez.api

import brave.SpanCustomizer
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.prez.api.dto.ErrorResponse
import com.prez.exception.NotFoundException
import com.prez.lib.tracing.SpanCustomization
import com.prez.ws.WebServiceException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ServerWebInputException

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ExceptionsHandler(private val spanCustomizer: SpanCustomizer) {


  /*
    Not catchable, due to null or missing values in json body and non-nullable properties in the request object.
    Missing values can be address with a "default value"
    null values cannot, unless you use a specific option of jackson-kotlin module
    https://github.com/FasterXML/jackson-module-kotlin/issues/87
    @ExceptionHandler(MissingKotlinParameterException::class)
    fun handleValidationException(ex: MissingKotlinParameterException): ResponseEntity<ErrorResponse> {
      val errorMessage = "1 error(s) while validating ${ex.pathReference} : ${ex.parameter} is missing"
      LOGGER.error(errorMessage, ex)
      SpanCustomization.tagError(spanCustomizer, ex)
      return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON)
        .body(ErrorResponse("VALIDATION_ERROR", errorMessage))
    }
  */
  @ExceptionHandler(WebExchangeBindException::class)
  fun handleValidationException(ex: WebExchangeBindException): ResponseEntity<ErrorResponse> {
    val errorMessage = processFieldsError(ex.bindingResult)
    LOGGER.error(errorMessage, ex)
    SpanCustomization.tagError(spanCustomizer, ex)
    return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON)
      .body(ErrorResponse("VALIDATION_ERROR", errorMessage))
  }

  private fun processFieldsError(errors: BindingResult): String {
    val messages = errors.fieldErrors.map { it.defaultMessage ?: it.toString() }.joinToString(",")
    return "${errors.fieldErrorCount} error(s) while validating ${errors.objectName} : $messages"
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleTypeMismatchException(ex: TypeMismatchException): ResponseEntity<ErrorResponse> {
    LOGGER.error(ex.localizedMessage, ex)
    SpanCustomization.tagError(spanCustomizer, ex)
    return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON)
      .body(ErrorResponse("VALIDATION_ERROR", ex.message!!))
  }


  @ExceptionHandler(WebServiceException::class)
  fun handleCustomerWSException(ex: WebServiceException): ResponseEntity<ErrorResponse> {
    LOGGER.error(ex.localizedMessage, ex)
    SpanCustomization.tagError(spanCustomizer, ex)
    return ResponseEntity.status(ex.httpStatusCode).contentType(APPLICATION_JSON)
      .body(ErrorResponse(ex.error.error, ex.error.errorDescription))
  }

  @ExceptionHandler(NotFoundException::class)
  fun elementNotFoundErrorHandler(ex: NotFoundException): ResponseEntity<ErrorResponse> {
    LOGGER.error(ex.localizedMessage, ex)
    return ResponseEntity.status(NOT_FOUND).contentType(APPLICATION_JSON)
      .body(ErrorResponse("NOT_FOUND", ex.localizedMessage))
  }

  @ExceptionHandler(ServerWebInputException::class)
  fun handleValidationException(ex: ServerWebInputException): ResponseEntity<ErrorResponse> {
    LOGGER.error(ex.localizedMessage, ex)
    SpanCustomization.tagError(spanCustomizer, ex)
    return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON)
      .body(ErrorResponse("VALIDATION_ERROR", ex.localizedMessage))
  }

  @ExceptionHandler(Exception::class)
  fun globalErrorHandler(ex: Exception): ResponseEntity<ErrorResponse> {
    LOGGER.error(ex.localizedMessage, ex)
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON)
      .body(
        ErrorResponse(
          "UNEXPECTED_ERROR",
          "Something horribly wrong happened, I could tell you what but then I’d have to kill you."
        )
      )
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(ExceptionsHandler::class.java)
  }
}