package com.prez.ws;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import com.prez.ws.model.GetCustomerWSResponse;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class CustomerWSClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerWSClient.class);

  private final RestOperations customerWebClient;
  private final CustomerWSProperties configuration;


  public CustomerWSClient(CustomerWSProperties configuration, RestOperations customerWebClient) {
    this.configuration = configuration;
    this.customerWebClient = customerWebClient;
  }

  @NewSpan("getCustomers")
  public Optional<GetCustomerWSResponse> getCustomer(final String customerId) {
    LOGGER.debug("Calling webservice GET {}/{}", configuration.getUrl(), customerId);
    final URI query = fromHttpUrl(configuration.getUrl()).path("/").pathSegment(customerId)
        .build(true)
        .toUri();
    try {
      ResponseEntity<GetCustomerWSResponse> response = customerWebClient
          .getForEntity(query, GetCustomerWSResponse.class);
      LOGGER.debug("Web service GET {}/{} reply is : {}", configuration.getUrl(), customerId, response.getStatusCode());

      // on pourrait mettre un interceptor et ça serait mieux mais pour comparer c'est plus simple :P
      switch (response.getStatusCode()) {
        case OK:
          return Optional.ofNullable(response.getBody());
        case NOT_FOUND:
          return Optional.empty();
        default:
          Object body = Optional.ofNullable((Object) response.getBody())
              .orElse("");
          throw new WebServiceException("CUSTOMER_WS_GET_CUSTOMER_ERROR",
              "GET CustomerWS", response.getStatusCode(),
              "Unexpected response from the server while retrieving customer for customerId="
                  + customerId + ", response=" + body);
      }
    } catch (Exception err) {
      LOGGER.error("CustomerWS getCustomer failed", err);
      throw err;
    }
  }

}

