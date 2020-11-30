package com.prez.api

import com.prez.api.dto.ErrorResponse
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.server.ServerWebInputException


abstract class ValidationHandler<T, U : Validator>(
  val validator: U
//val processBodyHandler: suspend (T, ServerRequest) -> ServerResponse,//aka KSuspendFunction2<T, ServerRequest, ServerResponse>,

) {

  abstract val processBodyHandler: suspend (ServerRequest, T) -> ServerResponse
  open val onValidationErrorsHandler: suspend (Errors, T, ServerRequest) -> ServerResponse =
    { errors, _, _ -> badRequest().bodyValueAndAwait(ErrorResponse("VALIDATION_ERROR", processFieldsError(errors))) }

  companion object {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }
}

// FIXME something is wrong with the inlining 
// java.lang.ClassCastException: class com.prez.api.dto.CreateCustomerPreferencesRequest cannot be cast to class org.springframework.web.reactive.function.server.ServerResponse (com.prez.api.dto.CreateCustomerPreferencesRequest and org.springframework.web.reactive.function.server.ServerResponse are in unnamed module of loader 'app')
suspend inline fun <reified T : Any, U : Validator> ValidationHandler<T, U>.handleRequest(
  request: ServerRequest,
  //processBodyHandler: suspend (T, ServerRequest) -> ServerResponse,//aka KSuspendFunction2<T, ServerRequest, ServerResponse>,
  //onValidationErrorsHandler: (Errors, T, ServerRequest) -> ServerResponse =
  //  { errors, _, _ -> throw ServerWebInputException(errors.allErrors.toString()) }
): ServerResponse {
  var body = request.awaitBodyOrNull<T>()
    ?: throw ServerWebInputException("Request body is mandatory") //FIXME c'est crade mais il n'y a pas de reification possible au niveau classe
  if (body !is T) { // this should never happen but happens anyway ^^°
    // FIXME to investigate ... we get this exception, when missing mandatory fields or when malformed JSON in the ServerRequest
    // Is this something due to inline and suspend function ?
    /* java.lang.ClassCastException: class kotlin.coroutines.intrinsics.CoroutineSingletons cannot be cast to class
     org.springframework.web.reactive.function.server.ServerResponse (kotlin.coroutines.intrinsics.CoroutineSingletons
     and org.springframework.web.reactive.function.server.ServerResponse are in unnamed module of loader 'app')
     */
    ValidationHandler.logger.error("Something went horribly wrong, here!!! Unexpected body type received : ${body.javaClass.name}");
    body = request.bodyToMono(Object::class.java).awaitFirstOrNull() as T /* FIXME ... apparently the awaitBodyOrNull got suspended while reading the body ?!?
    it is very unfortunate having to call bodyToMono again ^^° ... */
  }
  // sinon, il faut refactorer pour que ce ne soit pas une abstract class et faire de la composition ou du moins faire uniquement la validation sinon
  val errors: Errors = BeanPropertyBindingResult(body, T::class.java.name)
  validator.validate(body, errors)
  if (errors.allErrors.isEmpty()) {
    // TODO to investigate ... here we have to do the return here (not before the if statement) otherwise we also get the ClassCastException
     return processBodyHandler(request, body)
  }
  return onValidationErrorsHandler(errors, body, request)
}

private fun processFieldsError(errors: Errors): String {
  val messages = errors.fieldErrors.map { it.defaultMessage ?: "" }.joinToString(",")
  return "${errors.fieldErrorCount} error(s) while validating ${errors.objectName} : $messages"
}


