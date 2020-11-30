package com.prez.api

import com.prez.api.dto.CreateCustomerPreferencesRequest
import com.prez.extension.toCustomerPreferencesProfileResponse
import com.prez.service.CustomerService
import org.apache.commons.lang3.LocaleUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.awaitPrincipal
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.net.URI

@Component
class CreateCustomerPreferencesHandler(validator: Validator, val customerService: CustomerService) :
  ValidationHandler<CreateCustomerPreferencesRequest, Validator>(validator) {

  private val logger = LoggerFactory.getLogger(CreateCustomerPreferencesHandler::class.java)

  suspend fun processBody(
    originalRequest: ServerRequest,
    validBody: CreateCustomerPreferencesRequest
    ): ServerResponse {
    logger.info("createCustomer : {}", originalRequest.uri())
    val principal = originalRequest.awaitPrincipal()!!
    val created = customerService
      .saveCustomerPreferences(
        principal.name, validBody.seatPreference, validBody.classPreference,
        validBody.profileName, LocaleUtils.toLocale(validBody.language)
      ).toCustomerPreferencesProfileResponse()
    return created(URI.create("/customers/preferences/${created.id}")).bodyValueAndAwait(created)
  }

  override val processBodyHandler: suspend (ServerRequest, CreateCustomerPreferencesRequest) -> ServerResponse =
    ::processBody
  //{ validBody, originalRequest -> processBody(validBody, originalRequest) }

  // si on veut overrider override val onValidationErrorsHandler: (Errors, CreateCustomerPreferencesRequest, ServerRequest) -> ServerResponse = super.onValidationErrorsHandler
}