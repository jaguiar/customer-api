package com.prez.service;

import com.prez.cache.CustomerCacheRepository;
import com.prez.db.CustomerPreferencesRepository;
import com.prez.exception.NotFoundException;
import com.prez.model.Customer;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import com.prez.ws.CustomerWSClient;
import com.prez.ws.model.CreateCustomerPreferencesWSRequest;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CustomerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

  private final CustomerWSClient customerWebService;
  private final CustomerCacheRepository cache;
  private final CustomerWSResponseToCustomerMapper mapper;
  private final CustomerPreferencesRepository database;

  public CustomerService(CustomerWSClient customerWebService, CustomerCacheRepository customerCache,
                         CustomerWSResponseToCustomerMapper mapper, CustomerPreferencesRepository database) {
    this.customerWebService = customerWebService;
    this.cache = customerCache;
    this.mapper = mapper;
    this.database = database;
  }

  public Mono<Customer> getCustomerInfo(final String customerId) {
    LOGGER.debug("Getting customer with customerId = {}", customerId);
    return cache.findById(customerId)
        .switchIfEmpty(deferCallingCustomerWebService(customerId));
  }

  /**
   * Defer the execution of call to getCustomer web service. If you don't defer, the call will be executed in //
   * of the "previous" mono ( aka look in cache ) which is NOT what we want.
   * See https://stackoverflow.com/questions/54373920/mono-switchifempty-is-always-called if you want a more complete explanation
   */
  private Mono<Customer> deferCallingCustomerWebService(String customerId) {
    return Mono.defer(() ->
        customerWebService.getCustomer(customerId)
            .switchIfEmpty(Mono.error(new NotFoundException(customerId, "customer")))
            .map(mapper::toCustomer)
            .flatMap(customer ->
                cache.save(customer)
                    .defaultIfEmpty(false)
                    .doOnSuccess(savedInCache -> {
                      if (savedInCache) {
                        LOGGER.debug("Customer {} saved in cache", customer.getCustomerId());
                      } else {
                        LOGGER.error("COULD NOT SAVE {} in cache", customer.getCustomerId());
                      }
                    })
                    // then we just return the customer
                    .then(Mono.justOrEmpty(customer)))
    );
  }

  public Mono<CustomerPreferences> createCustomerPreferences(
      final String customerId,
      final String seatPreference,
      final Integer classPreference,
      final String profileName,
      final Locale language) {
    LOGGER.debug("createCustomerPreferences : seatPreference \"{}\", classPreference \"{}\" and profileName \"{}\" with locale\"{}\" for customer \"{}\"",
        seatPreference, classPreference, profileName, language, customerId);
    CreateCustomerPreferencesWSRequest createCustomerPreferencesRequest =
        new CreateCustomerPreferencesWSRequest(seatPreference, classPreference, profileName);
    return customerWebService.createCustomerPreferences(customerId, createCustomerPreferencesRequest, language)
        .map(response -> CustomerPreferences.builder()
            .id(response.getId())
            .customerId(customerId)
            .seatPreference(SeatPreference.valueOf(response.getSeatPreference()))
            .classPreference(response.getClassPreference())
            .profileName(response.getProfileName())
            .build()
        );
  }

  public Mono<CustomerPreferences> saveCustomerPreferences(String customerId, SeatPreference seatPreference,
                                                           Integer classPreference, String profileName, Locale language) {
    LOGGER.debug("saveCustomerPreferences : " +
            "seatPreference \"{}\", classPreference \"{}\" and profileName \"{}\"" +
            " with locale\"{}\" for customer \"{}\"",
        seatPreference, classPreference, profileName, language, customerId);
    final CustomerPreferences createCustomerPreferencesRequest = CustomerPreferences.builder()
        .id(UUID.randomUUID().toString())
        .customerId(customerId)
        .seatPreference(Optional.ofNullable(seatPreference).orElse(null))
        .classPreference(classPreference)
        .profileName(profileName)
        .language(language)
        .build();
    return database.save(createCustomerPreferencesRequest);
  }

  public Flux<CustomerPreferences> getCustomerPreferences(String customerId) {
    LOGGER.debug("getCustomerPreferences for customer \"{}\"", customerId);
    return database.findByCustomerId(customerId);
  }
}
