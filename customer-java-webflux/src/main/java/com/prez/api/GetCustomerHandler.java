package com.prez.api;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.prez.api.dto.CustomerResponse;
import com.prez.service.CustomerService;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.Loggers;

/**
 * This is the equivalent of our RestTemplate controller
 */
@Component
public class GetCustomerHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCustomerHandler.class);

  private final CustomerService customerService;

  public GetCustomerHandler(CustomerService customerService) {
    this.customerService = customerService;
  }

  @CrossOrigin
  public Mono<ServerResponse> getCustomer(ServerRequest request) {
    return request.principal()
        .flatMap(principal -> customerService.getCustomerInfo(principal.getName()))
        .map(CustomerResponse::of)
        .doOnSuccess(response -> LOGGER.debug("Response: {}", response))
        .flatMap(ok()::bodyValue)
        .log(Loggers.getLogger(GetCustomerHandler.class), Level.FINE, true);
  }
}
