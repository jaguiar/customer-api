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
import org.springframework.web.reactive.function.server.json
import reactor.core.publisher.Mono
import reactor.util.Loggers
import java.util.logging.Level

/**
 * This is the equivalent of our RestTemplate controller
 */
@Component
class GetCustomerPreferencesHandler(private val customerService: CustomerService) {

  private val logger = LoggerFactory.getLogger(GetCustomerPreferencesHandler::class.java)


  @CrossOrigin
  fun getCustomerPreferences(request: ServerRequest): Mono<ServerResponse> {
    return request.principal()
        .flatMap { principalToken -> customerService.getCustomerPreferences(principalToken.name) }
        .map { toCustomerPreferencesResponse(it) }
        .doOnSuccess { response -> logger.debug("Response: $response") }
        .flatMap(ok().json()::bodyValue)
        .log(Loggers.getLogger(GetCustomerPreferencesHandler::class.java), Level.FINE, true)
  }

  private fun toCustomerPreferencesResponse(profiles: List<CustomerPreferences>): CustomerPreferencesResponse {
    val profilesResponse = profiles.map { it.toCustomerPreferencesProfileResponse() }.toList()
    return CustomerPreferencesResponse(profilesResponse);
  }
}
