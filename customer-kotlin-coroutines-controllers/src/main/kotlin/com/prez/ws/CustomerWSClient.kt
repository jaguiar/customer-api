package com.prez.ws

import com.prez.ws.model.CreateCustomerPreferencesWSRequest
import com.prez.ws.model.CreateCustomerPreferencesWSResponse
import com.prez.ws.model.GetCustomerWSResponse
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.annotation.NewSpan
import org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.util.Locale

interface CustomerClient {
  suspend fun getCustomer(customerId: String): GetCustomerWSResponse?
  suspend fun createCustomerPreferences(
    customerId: String,
    createCustomerPreferencesRequest: CreateCustomerPreferencesWSRequest,
    language: Locale?
  ): CreateCustomerPreferencesWSResponse
}

open class CustomerWSClient(
  private val configuration: CustomerWSProperties,
  private val webClient: WebClient
) : CustomerClient {

  companion object {
    private val logger = LoggerFactory.getLogger(CustomerWSClient::class.java)
    private val defaultLanguageValue = Locale.FRENCH
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
      // FIXME on va mettre une exchange filter function ici ça va etre bien
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

  @NewSpan("postCustomerPreferences")
  override suspend fun createCustomerPreferences(
    customerId: String,
    createCustomerPreferencesRequest: CreateCustomerPreferencesWSRequest,
    language: Locale?
  ): CreateCustomerPreferencesWSResponse {
    logger.debug("Calling webservice POST  ${configuration.url}/$customerId/preferences")
    val languageOrDefault = language ?: defaultLanguageValue

    val query = fromHttpUrl(configuration.url)
      .pathSegment(customerId)
      .pathSegment("/preferences")
      .build(true)
      .toUri()
    try {
      val response: CreateCustomerPreferencesWSResponse = webClient
        .post()
        .uri(query)
        .body(fromValue(createCustomerPreferencesRequest))
        .header(ACCEPT_LANGUAGE, languageOrDefault.language)
        .retrieve()
        .awaitBody()
      logger.debug("POST CustomerWS preferences for customer $customerId : $response")
      return response
    } catch (e: Exception) {
      // FIXME on va mettre une exchange filter function ici ça va etre bien
      logger.error("CustomerWS createCustomerPreferences for $customerId failed", e)
      when {
        e is WebClientResponseException -> throw
        WebServiceException(
          "CUSTOMER_WS_CREATE_CUSTOMER_PREFERENCES_ERROR", "POST CustomerWS", e.statusCode,
          "Unable to create customer for customerId=$customerId ${e.responseBodyAsString}"
        )
        else -> throw
        WebServiceException(
          "CUSTOMER_WS_CREATE_CUSTOMER_PREFERENCES_ERROR",
          "POST CustomerWS",
          INTERNAL_SERVER_ERROR,
          "Unexpected error : ${e.message} for customerId=$customerId"
        )
      }
    }
  }
}