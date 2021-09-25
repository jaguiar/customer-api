package com.prez.api

import com.prez.api.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

abstract class AbstractValidationHandler<T, U : Validator> protected constructor(
    private val validationClass: Class<T>,
    private val validator: U
) {

  fun handleRequest(request: ServerRequest): Mono<ServerResponse> {
    return request.bodyToMono(validationClass)
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is mandatory")))
        .flatMap { body ->
          val errors: Errors = BeanPropertyBindingResult(body, validationClass.name)
          validator.validate(body, errors)
          if (errors.allErrors.isEmpty()) {
            processBody(body, request)
          } else {
            onValidationErrors(errors, body, request)
          }
        }
  }

  protected fun onValidationErrors(errors: Errors, invalidBody: T, request: ServerRequest): Mono<ServerResponse> {
    return badRequest().bodyValue(ErrorResponse("VALIDATION_ERROR", processFieldsError(errors)))
  }

  protected abstract fun processBody(validBody: T, originalRequest: ServerRequest): Mono<ServerResponse>

  private fun processFieldsError(errors: Errors): String {
    val messages = errors.fieldErrors.map { it.defaultMessage ?: "" }.joinToString(",")
    return "${errors.fieldErrorCount} error(s) while validating ${errors.objectName} : $messages"
  }
}