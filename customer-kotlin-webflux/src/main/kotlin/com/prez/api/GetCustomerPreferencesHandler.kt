package com.prez.api

import com.prez.api.dto.CustomerPreferencesProfileResponse
import com.prez.exception.NotFoundException
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

/**
 * This is the equivalent of our RestTemplate controller
 */
@Component
class GetCustomerPreferencesHandler(private val customerService: CustomerService) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(GetCustomerPreferencesHandler::class.java)
  }

  @CrossOrigin
  fun getCustomerPreferences(request: ServerRequest): Mono<ServerResponse> {
    return request.principal()
      .flatMap { principalToken ->
        val preferences = customerService.getCustomerPreferences(principalToken.name)
        preferences.hasElements()
          .flatMap {
            if (it) ok().json().body(preferences.map(CustomerPreferences::toCustomerPreferencesProfileResponse), CustomerPreferencesProfileResponse::class.java)
            else Mono.error(NotFoundException(principalToken.name, "customer"))
          }
      }

  }

}
