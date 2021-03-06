package com.prez.ws;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import com.prez.ws.model.GetCustomerWSResponse;
import java.net.URI;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.Loggers;

public class CustomerWSClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerWSClient.class);

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
          // on pourrait mettre une exchange filter function ici et ça serait mieux, mais c'est plus simple pour comparer :P
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
}

