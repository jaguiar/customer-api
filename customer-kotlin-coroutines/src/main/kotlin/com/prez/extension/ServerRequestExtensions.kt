package com.prez.extension

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.server.ServerWebInputException

//il n'y a pas de reification possible au niveau classe
suspend inline fun <reified T : Any, U : Validator> ServerRequest.awaitBodyAndValidate(
  validator: U
): T {
  awaitBodyOrNull<T>()
    ?.let { body ->
      val errors: Errors = BeanPropertyBindingResult(body, T::class.java.name)
      validator.validate(body, errors)
      if (errors.allErrors.isEmpty()) {
        return body
      }
      val messages = errors.fieldErrors.map { it.defaultMessage ?: it.toString() }.joinToString(",")
      throw ServerWebInputException("${errors.fieldErrorCount} error(s) while validating ${errors.objectName} : $messages")
    }
    ?: throw ServerWebInputException("Request body is mandatory")
}