package com.prez.api

import com.prez.api.dto.CustomerPreferencesResponse
import com.prez.extension.toCustomerPreferencesProfileResponse
import com.prez.model.CustomerPreferences
import com.prez.service.CustomerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitPrincipal
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.json

/**
 * This is the equivalent of our RestTemplate controller
 */
@Component
class GetCustomerPreferencesHandler(private val customerService: CustomerService) {

  private val logger = LoggerFactory.getLogger(GetCustomerPreferencesHandler::class.java)


  @CrossOrigin
  suspend fun getCustomerPreferences(request: ServerRequest): ServerResponse {
    val principal = request.awaitPrincipal()!!
    val customerPreferences = customerService.getCustomerPreferences(principal.name)
    logger.debug("Response: $customerPreferences")
    return ok().json().bodyValueAndAwait(toCustomerPreferencesResponse(customerPreferences))
  }

  private fun toCustomerPreferencesResponse(profiles: List<CustomerPreferences>): CustomerPreferencesResponse {
    val profilesResponse = profiles.map { it.toCustomerPreferencesProfileResponse() }.toList()
    return CustomerPreferencesResponse(profilesResponse);
  }
}
