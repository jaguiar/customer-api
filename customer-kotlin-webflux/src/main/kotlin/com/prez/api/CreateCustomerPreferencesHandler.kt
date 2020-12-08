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
import reactor.core.publisher.Mono
import reactor.util.Loggers
import java.net.URI
import java.util.logging.Level

@Component
class CreateCustomerPreferencesHandler(
  validator: Validator,
  private val customerService: CustomerService
) :
  AbstractValidationHandler<CreateCustomerPreferencesRequest, Validator>(
    CreateCustomerPreferencesRequest::class.java,
    validator
  ) {

  companion object {
    private val logger = LoggerFactory.getLogger(CreateCustomerPreferencesHandler::class.java)
  }

  override fun processBody(
    validBody: CreateCustomerPreferencesRequest,
    originalRequest: ServerRequest
  ): Mono<ServerResponse> {
    logger.info("CreateCustomerPreferences : {}", originalRequest.uri())
    return originalRequest.principal()
      .flatMap { principal ->
        customerService.createCustomerPreferences(
          principal.name, validBody.seatPreference, validBody.classPreference,
          validBody.profileName, LocaleUtils.toLocale(validBody.language)
        )
      }
      .map { it.toCustomerPreferencesProfileResponse() }
      .flatMap { created -> created(URI.create("/customers/preferences/${created.id}")).bodyValue(created) }
      .log(Loggers.getLogger(GetCustomerPreferencesHandler::class.java), Level.FINE, true)
  }
}