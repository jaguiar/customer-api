package com.prez.ws

import com.prez.ws.model.GetCustomerWSResponse
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.annotation.NewSpan
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl

interface CustomerClient {
  suspend fun getCustomer(customerId: String): GetCustomerWSResponse?
}

open class CustomerWSClient(
  private val configuration: CustomerWSProperties,
  private val webClient: WebClient
) : CustomerClient {

  companion object {
    private val logger = LoggerFactory.getLogger(CustomerWSClient::class.java)
    private const val webServiceName = "CUSTOMER_WS"
  }

  @NewSpan("getCustomer")
  override suspend fun getCustomer(customerId: String): GetCustomerWSResponse? {
    logger.debug("Calling webservice GET ${configuration.url}/$customerId")
    val query = fromHttpUrl(configuration.url).path("/").pathSegment(customerId)
      .build(true)
      .toUri()

    try {
      val responseGet: GetCustomerWSResponse = webClient
        .get()
        .uri(query)
        .retrieve()
        .onStatus(HttpStatus::is3xxRedirection) { it.createException() } // Gestion du cas 3xx
        .awaitBody()
      logger.debug("GET CustomerWS retrieved customer : $responseGet")
      return responseGet
    } catch (e: Exception) {
      // on pourrait mettre une exchange filter function ici et ça serait mieux, mais c'est plus simple pour comparer :P
      logger.error("CustomerWS getCustomer failed", e)
      when { // and clearly you would love a guard condition :Pi
        e is WebClientResponseException && e.statusCode == NOT_FOUND -> return null
        e is WebClientResponseException -> throw WebServiceException(
          "CUSTOMER_WS_GET_CUSTOMER_ERROR", webServiceName, e.statusCode,
          "Unexpected response from the server while retrieving customer for customerId=$customerId, response=${e.responseBodyAsString}"
        )
        else -> throw WebServiceException(
          "CUSTOMER_WS_GET_CUSTOMER_ERROR",
          webServiceName,
          INTERNAL_SERVER_ERROR,
          "Unexpected error : ${e.message} for customerId=$customerId"
        )
      }
    }
  }
}