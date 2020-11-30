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
import org.springframework.web.reactive.function.server.awaitPrincipal
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.json

/**
 * This is the equivalent of our RestTemplate controller
 */
@Component
class GetCustomerHandler(private val customerService: CustomerService) {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(GetCustomerHandler::class.java)
  }

  @CrossOrigin
  suspend fun getCustomer(request: ServerRequest): ServerResponse {
    val principal = request.awaitPrincipal()!!
    val response = customerService.getCustomerInfo(principal.name)
    logger.debug("Response: $response")
    return ok().json().bodyValueAndAwait(response.toCustomerResponse())
  }
}
