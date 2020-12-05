package com.prez.ws

import com.prez.ws.model.GetCustomerWSResponse
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.annotation.NewSpan
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import reactor.core.publisher.Mono
import reactor.util.Loggers
import java.util.logging.Level

interface CustomerClient {
  fun getCustomer(customerId: String): Mono<GetCustomerWSResponse>
}

open class CustomerWSClient(
  private val configuration: CustomerWSProperties,
  private val webClient: WebClient
) : CustomerClient {

  private val logger = LoggerFactory.getLogger(CustomerWSClient::class.java)

  private val webServiceName = "CUSTOMER_WS"

  @NewSpan("getCustomer")
  override fun getCustomer(customerId: String): Mono<GetCustomerWSResponse> {
    logger.debug("Calling webservice GET ${configuration.url}/$customerId")
    val query = fromHttpUrl(configuration.url).path("/").pathSegment(customerId)
      .build(true)
      .toUri()

    return webClient
      .get()
      .uri(query)
      .retrieve()
      .onStatus(HttpStatus::is3xxRedirection) { it.createException() } // Gestion du cas 3xx
      .bodyToMono(GetCustomerWSResponse::class.java)
      .doOnSuccess { logger.debug("GET CustomerWS retrieved customer : $it") }
      // on pourrait mettre une exchange filter function ici et ça serait mieux, mais c'est plus simple pour comparer :P
      .onErrorResume {
        logger.error("CustomerWS getCustomer failed", it)
        when { // and clearly you would love a guard condition :P
          it is WebClientResponseException && it.statusCode == NOT_FOUND -> Mono.empty()
          it is WebClientResponseException -> Mono.error(
            WebServiceException(
              "CUSTOMER_WS_GET_CUSTOMER_ERROR", webServiceName, it.statusCode,
              "Unexpected response from the server while retrieving customer for customerId=$customerId, response=${it.responseBodyAsString}"
            )
          )
          else -> Mono.error(
            WebServiceException(
              "CUSTOMER_WS_GET_CUSTOMER_ERROR",
              webServiceName,
              INTERNAL_SERVER_ERROR,
              "Unexpected error : ${it.message} for customerId=$customerId"
            )
          )
        }
      }
      .log(Loggers.getLogger(CustomerWSClient::class.java), Level.FINE, true)
  }
}