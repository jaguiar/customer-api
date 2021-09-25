package com.prez.api

import com.prez.api.dto.ErrorResponse
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.server.ServerWebInputException

/**
 * Handler that performs input validation
 */
abstract class AbstractValidationHandler<T, U : Validator> protected constructor(
    val validator: U
) {

  /* That's a lot of code to inline ! */
  suspend inline fun <reified R : T> handleRequest(request: ServerRequest): ServerResponse {
    val body: R? = request.awaitBodyOrNull() ?: throw ServerWebInputException("Request body is mandatory")

    body.let { body ->
      val errors: Errors = BeanPropertyBindingResult(body, R::class.java.name)
      validator.validate(body, errors)
      return if (errors.allErrors.isEmpty()) {
        processBody(body!!, request)
      } else {
        onValidationErrors(errors, body!!, request);
      }
    }
  }

  /*
    The difference with the kotlin Webflux version is that the onValidationErrors method can no longer be protected because
    a protected method cannot be called from a public-API inline function
   */
  suspend fun onValidationErrors(errors: Errors, invalidBody: T, request: ServerRequest): ServerResponse {
    return badRequest().bodyValueAndAwait(ErrorResponse("VALIDATION_ERROR", processFieldsError(errors)))
  }

  /*
    The difference with the kotlin Webflux version is that the processBody method can no longer be protected because
    a protected method cannot be called from a public-API inline function
   */
  abstract suspend fun processBody(validBody: T, originalRequest: ServerRequest): ServerResponse

  private fun processFieldsError(errors: Errors): String {
    val messages = errors.fieldErrors.map { it.defaultMessage ?: "" }.joinToString(",")
    return "${errors.fieldErrorCount} error(s) while validating ${errors.objectName} : $messages"
  }
}