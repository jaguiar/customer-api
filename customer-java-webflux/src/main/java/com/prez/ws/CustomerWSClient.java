package com.prez.ws;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import com.prez.ws.model.CreateCustomerPreferencesWSRequest;
import com.prez.ws.model.CreateCustomerPreferencesWSResponse;
import com.prez.ws.model.GetCustomerWSResponse;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.Loggers;

public class CustomerWSClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerWSClient.class);
  private final static Locale DEFAULT_LANGUAGE_VALUE = Locale.FRENCH;

  private final WebClient customerWebClient;
  private final CustomerWSProperties configuration;


  public CustomerWSClient(CustomerWSProperties configuration, WebClient customerWebClient) {
    this.configuration = configuration;
    this.customerWebClient = customerWebClient;
  }

  @NewSpan("getCustomers")
  public Mono<GetCustomerWSResponse> getCustomer(final String customerId) {
    LOGGER.debug("Calling webservice GET {}/{}", configuration.getUrl(), customerId);
    final URI query = fromHttpUrl(configuration.getUrl()).path("/").pathSegment(customerId)
        .build(true)
        .toUri();

    return customerWebClient
        .get()
        .uri(query)
        .exchange()
        .doOnError(err ->
            LOGGER.error("CustomerWS getCustomer failed", err))
        .flatMap(response -> {
          LOGGER.debug("Web service GET {}/{} reply is : {}", configuration.getUrl(), customerId, response.statusCode());
          switch (response.statusCode()) {
            case OK:
              return response.bodyToMono(GetCustomerWSResponse.class)
                  .switchIfEmpty(Mono.error(new WebServiceException("CUSTOMER_WS_GET_CUSTOMER_ERROR", "GET CustomerWS",
                      OK, "Unable to map response body for customerId=" + customerId)))  // should never happen, but ...
                  .doOnSuccess(r -> LOGGER.debug("GET CustomerWS retrieved customer : {}", r))
                  .doOnError(err -> LOGGER.error("GET CustomerWS returned error: ", err));
            case NOT_FOUND:
              return response.bodyToMono(Object.class) // even if the call failed we still need to read the body
                  .defaultIfEmpty("")
                  .flatMap(o -> Mono.empty());
            default:
              // even if the call failed we still need to read the body
              return response.bodyToMono(String.class)
                  .defaultIfEmpty("")
                  .flatMap(body -> Mono.error(new WebServiceException("CUSTOMER_WS_GET_CUSTOMER_ERROR",
                      "GET CustomerWS", response.statusCode(),
                      "Unexpected response from the server while retrieving customer for customerId="
                          + customerId + ", response=" + body)));
          }
        })
        .log(Loggers.getLogger(CustomerWSClient.class), Level.FINE, true);

  }

  @NewSpan("postCustomerPreferences")
  public Mono<CreateCustomerPreferencesWSResponse> createCustomerPreferences(String customerId,
                                                                             CreateCustomerPreferencesWSRequest createCustomerPreferencesWSRequest,
                                                                             Locale language) {
    LOGGER.debug("Calling webservice POST  {}/{}/preferences", configuration.getUrl(), customerId);
    final Locale languageOrDefault = Optional.ofNullable(language).orElse(DEFAULT_LANGUAGE_VALUE);

    final URI query = fromHttpUrl(configuration.getUrl())
        .pathSegment(customerId)
        .pathSegment("preferences")
        .build(true)
        .toUri();

    return customerWebClient
        .post()
        .uri(query)
        .body(fromValue(createCustomerPreferencesWSRequest))
        .header(ACCEPT_LANGUAGE, languageOrDefault.getLanguage())
        .retrieve()
        .bodyToMono(CreateCustomerPreferencesWSResponse.class)
        .doOnSuccess(resp -> LOGGER.debug("POST CustomerWS preferences for customer {} : {}", customerId, resp))
        .onErrorResume(err -> {
          LOGGER.error("CustomerWS createCustomerPreferences for customerId=" + customerId + "failed", err);
          if (err instanceof WebClientResponseException) {
            WebClientResponseException it = (WebClientResponseException) err;
            return Mono.error(new WebServiceException(
                "CUSTOMER_WS_CREATE_CUSTOMER_PREFERENCES_ERROR", "POST CustomerWS", it.getStatusCode(),
                "Unable to create customer for customerId=" + customerId + it.getResponseBodyAsString()
            ));
          } //else
          return Mono.error(new WebServiceException(
              "CUSTOMER_WS_CREATE_CUSTOMER_PREFERENCES_ERROR", "POST CustomerWS", INTERNAL_SERVER_ERROR,
              "Unexpected error : " + err.getMessage() + "for customerId=" + customerId
          ));
        });
  }
}

