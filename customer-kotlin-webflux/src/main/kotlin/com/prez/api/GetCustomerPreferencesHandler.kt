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
    /* v1
     return request.principal()
       .map { principalToken ->
         customerService.getCustomerPreferences(principalToken.name)
           .map(CustomerPreferences::toCustomerPreferencesProfileResponse)
       }
       .flatMap { preferences ->
         preferences.hasElements().flatMap {
           if (it) ok().json().body(preferences, CustomerPreferencesProfileResponse::class.java)
           else Mono.error(NotFoundException("","customer"))//status(HttpStatus.NOT_FOUND).bodyValue(ErrorResponse("NOT_FOUND", "ex.localizedMessage"))
         }
       }.log(Loggers.getLogger(GetCustomerPreferencesHandler::class.java), Level.FINE, true)
     */
/*
    return request.principal()
      .map { principalToken -> customerService.getCustomerPreferences(principalToken.name) }
      .flatMap { preferences ->
        preferences.hasElements().flatMap {
          if (it) ok().json().body(
            preferences.map(CustomerPreferences::toCustomerPreferencesProfileResponse),
            CustomerPreferencesProfileResponse::class.java
          )
          else Mono.error(NotFoundException("", "customer"))
        }
      }.log(Loggers.getLogger(GetCustomerPreferencesHandler::class.java), Level.FINE, true)
    */
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
