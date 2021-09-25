package com.prez.api

import com.prez.api.dto.CreateCustomerPreferencesRequest
import com.prez.extension.awaitBodyAndValidate
import com.prez.extension.toCustomerPreferencesProfileResponse
import com.prez.service.CustomerService
import kotlinx.coroutines.FlowPreview
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
// version with ServerRequest extension function
class CreateCustomerPreferencesHandler(val validator: Validator, val customerService: CustomerService) {
/* // version with the AbstractValidationHandler => we no longer need the class type
class CreateCustomerPreferencesHandler(validator: Validator, val customerService: CustomerService)
  : AbstractValidationHandler<CreateCustomerPreferencesRequest, Validator>(validator) { */

  private val logger = LoggerFactory.getLogger(CreateCustomerPreferencesHandler::class.java)

  /* version with ServerRequest extension function */
  suspend fun createCustomerPreferences(
    originalRequest: ServerRequest
  ): ServerResponse {
    logger.info("createCustomer : {}", originalRequest.uri())
    val principal = originalRequest.awaitPrincipal()!!

    val validBody: CreateCustomerPreferencesRequest =
      originalRequest.awaitBodyAndValidate(validator)
    val created = customerService
      .createCustomerPreferences(
        principal.name, validBody.seatPreference, validBody.classPreference,
        validBody.profileName, LocaleUtils.toLocale(validBody.language)
      ).toCustomerPreferencesProfileResponse()
    return created(URI.create("/customers/preferences/${created.id}")).bodyValueAndAwait(created)
  }

  /* // version with the AbstractValidationHandler
  override suspend fun processBody(
    validBody: CreateCustomerPreferencesRequest,
    originalRequest: ServerRequest
  ): ServerResponse {
    logger.info("createCustomer : {}", originalRequest.uri())
    val principal = originalRequest.awaitPrincipal()!!
    val created = customerService
      .createCustomerPreferences(
        principal.name, validBody.seatPreference, validBody.classPreference,
        validBody.profileName, LocaleUtils.toLocale(validBody.language)
      ).toCustomerPreferencesProfileResponse()
    return created(URI.create("/customers/preferences/${created.id}")).bodyValueAndAwait(created)
  } */
}