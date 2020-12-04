package com.prez.api

import com.prez.extension.toCustomerPreferencesProfileResponse
import com.prez.model.CustomerPreferences
import com.prez.service.CustomerService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitPrincipal
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.json

/**
 * This is the equivalent of our RestTemplate controller
 */
@Component
class GetCustomerPreferencesHandler(private val customerService: CustomerService) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(GetCustomerPreferencesHandler::class.java)
  }

  @FlowPreview
  @CrossOrigin
  suspend fun getCustomerPreferences(request: ServerRequest): ServerResponse {
    val principal = request.awaitPrincipal()!!
    LOGGER.info("getCustomerPreferences for user: {}", principal.name)
    val customerPreferencesProfileResponses = customerService.getCustomerPreferences(principal.name)
      .map(CustomerPreferences::toCustomerPreferencesProfileResponse)
    return ok().json().bodyAndAwait(customerPreferencesProfileResponses)
  }
}
