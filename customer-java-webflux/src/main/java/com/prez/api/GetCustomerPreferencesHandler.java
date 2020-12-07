package com.prez.api;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

import com.prez.api.dto.CustomerPreferencesProfileResponse;
import com.prez.exception.NotFoundException;
import com.prez.model.CustomerPreferences;
import com.prez.service.CustomerService;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Loggers;

@Component
public class GetCustomerPreferencesHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCustomerPreferencesHandler.class);
  private final CustomerService customerService;

  public GetCustomerPreferencesHandler(CustomerService customerService) {
    this.customerService = customerService;
  }

  @CrossOrigin
  public Mono<ServerResponse> getCustomerPreferences(ServerRequest request) {
    LOGGER.info("getCustomerPreferences : {}", request.uri());
    return request.principal()
        .flatMap(principalToken -> {
          final Flux<CustomerPreferences> preferences = customerService.getCustomerPreferences(principalToken.getName());
          return preferences.hasElements()
              .flatMap(hasElement -> hasElement ?
                  ok().contentType(APPLICATION_JSON).body(preferences.map(CustomerPreferencesProfileResponse::of), CustomerPreferencesProfileResponse.class)
                  : Mono.error(new NotFoundException(principalToken.getName(), "customer"))
              );
            })
        .log(Loggers.getLogger(CreateCustomerPreferencesHandler.class), Level.FINE, true);
  }


}
