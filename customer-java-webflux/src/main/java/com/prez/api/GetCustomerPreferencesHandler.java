package com.prez.api;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.prez.api.dto.CustomerPreferencesProfileResponse;
import com.prez.api.dto.CustomerPreferencesResponse;
import com.prez.model.CustomerPreferences;
import com.prez.service.CustomerService;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
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
        .flatMap(principalToken -> customerService.getCustomerPreferences(principalToken.getName()))
        .map(this::toCustomerPreferencesResponse)
        .flatMap(created -> ok().bodyValue(created))
        .log(Loggers.getLogger(CreateCustomerPreferencesHandler.class), Level.FINE, true);
  }

  private CustomerPreferencesResponse toCustomerPreferencesResponse(List<CustomerPreferences> customerPreferences) {
    List<CustomerPreferencesProfileResponse> profiles = customerPreferences.stream()
        .map(profiled -> CustomerPreferencesProfileResponse.builder()
            .id(profiled.getId())
            .customerId(profiled.getCustomerId())
            .seatPreference(profiled.getSeatPreference())
            .classPreference(profiled.getClassPreference())
            .profileName(profiled.getProfileName())
            .language(profiled.getLanguage())
            .build()).collect(toList());
    return CustomerPreferencesResponse.builder().profiles(profiles).build();
  }
}
