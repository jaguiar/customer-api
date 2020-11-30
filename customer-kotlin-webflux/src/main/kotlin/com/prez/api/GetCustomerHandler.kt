package com.prez.api

import com.prez.extension.toCustomerResponse
import com.prez.service.CustomerService
import org.slf4j.Logger
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
class GetCustomerHandler(private val customerService: CustomerService) {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(GetCustomerHandler::class.java)
  }

  @CrossOrigin
  fun getCustomer(request: ServerRequest): Mono<ServerResponse> {
    return request.principal()
        .flatMap { principal -> customerService.getCustomerInfo(principal.name) }
        .map { it.toCustomerResponse() }
        .doOnSuccess { response -> logger.debug("Response: $response") }
        .flatMap(ok().json()::bodyValue)
        .log(Loggers.getLogger(GetCustomerHandler::class.java), Level.FINE, true)
  }
}
