package com.prez.api;

import static org.springframework.web.reactive.function.server.ServerResponse.created;

import com.prez.api.dto.CreateCustomerPreferencesRequest;
import com.prez.api.dto.CustomerPreferencesProfileResponse;
import com.prez.service.CustomerService;
import java.net.URI;
import java.util.logging.Level;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.Loggers;

@Component
public class CreateCustomerPreferencesHandler
    extends AbstractValidationHandler<CreateCustomerPreferencesRequest, Validator> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateCustomerPreferencesHandler.class);
  private final CustomerService customerService;

  public CreateCustomerPreferencesHandler(Validator validator, CustomerService customerService) {
    super(CreateCustomerPreferencesRequest.class, validator);
    this.customerService = customerService;
  }

  @Override
  protected Mono<ServerResponse> processBody(CreateCustomerPreferencesRequest validBody, ServerRequest originalRequest) {
    LOGGER.info("CreateCustomerPreferences : {}", originalRequest.uri());
    return originalRequest.principal()
        .flatMap(principal -> customerService
            .saveCustomerPreferences(principal.getName(), validBody.getSeatPreference(), validBody.getClassPreference(),
                validBody.getProfileName(), LocaleUtils.toLocale(validBody.getLanguage())))
        .map(customerPreferences -> CustomerPreferencesProfileResponse.builder()
            .id(customerPreferences.getId())
            .customerId(customerPreferences.getCustomerId())
            .seatPreference(customerPreferences.getSeatPreference())
            .classPreference(customerPreferences.getClassPreference())
            .profileName(customerPreferences.getProfileName())
            .language(customerPreferences.getLanguage())
            .build())
        .flatMap(created -> created(URI.create("/customers/preferences/" + created.getId())).bodyValue(created))
        .log(Loggers.getLogger(CreateCustomerPreferencesHandler.class), Level.FINE, true);
  }
}
