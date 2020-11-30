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
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import reactor.core.publisher.Mono
import reactor.util.Loggers
import java.util.Locale
import java.util.Locale.FRENCH
import java.util.logging.Level

interface CustomerClient {
  fun getCustomer(customerId: String): Mono<GetCustomerWSResponse>
  fun createCustomerPreferences(
      customerId: String,
      createCustomerPreferencesRequest: CreateCustomerPreferencesWSRequest,
      language: Locale?
  ): Mono<CreateCustomerPreferencesWSResponse>
}

open class CustomerWSClient(
    private val configuration: CustomerWSProperties,
    private val webClient: WebClient
) : CustomerClient {

  private val logger = LoggerFactory.getLogger(CustomerWSClient::class.java)

  private val defaultLanguageValue = FRENCH

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

  @NewSpan("postCustomerPreferences")
  override fun createCustomerPreferences(
      customerId: String,
      createCustomerPreferencesRequest: CreateCustomerPreferencesWSRequest,
      language: Locale?
  ): Mono<CreateCustomerPreferencesWSResponse> {
    logger.debug("Calling webservice POST  ${configuration.url}/$customerId/preferences")
    val languageOrDefault = language ?: defaultLanguageValue

    val query = fromHttpUrl(configuration.url)
        .pathSegment(customerId)
        .pathSegment("/preferences")
        .build(true)
        .toUri()

    return webClient
        .post()
        .uri(query)
        .body(fromValue(createCustomerPreferencesRequest))
        .header(ACCEPT_LANGUAGE, languageOrDefault.language)
        .retrieve()
        .bodyToMono(CreateCustomerPreferencesWSResponse::class.java)
        .doOnSuccess { logger.debug("POST CustomerWS preferences for customer $customerId : $it") }
        .onErrorResume {
          logger.error("CustomerWS createCustomerPreferences for $customerId failed", it)
          when {
            it is WebClientResponseException -> Mono.error(
                WebServiceException( // FIXME on devrait peut-être trouver un code erreur plus court ^^
                    "CUSTOMER_WS_CREATE_CUSTOMER_PREFERENCES_ERROR", "POST CustomerWS", it.statusCode,
                    "Unable to create customer for customerId=$customerId ${it.responseBodyAsString}"
                )
            )
            else -> Mono.error(
                WebServiceException(
                    "CUSTOMER_WS_CREATE_CUSTOMER_PREFERENCES_ERROR",
                    "POST CustomerWS",
                    INTERNAL_SERVER_ERROR,
                    "Unexpected error : ${it.message} for customerId=$customerId"
                )
            )
          }
        }
        .log(Loggers.getLogger(CustomerWSClient::class.java), Level.FINE, true)
  }
}