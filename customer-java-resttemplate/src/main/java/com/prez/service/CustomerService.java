package com.prez.service;

import com.prez.cache.CustomerCacheRepository;
import com.prez.db.CustomerPreferencesRepository;
import com.prez.exception.NotFoundException;
import com.prez.model.Customer;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import com.prez.ws.CustomerWSClient;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

  /*Historiquement, il y avait un circuit breaker*/
  public Customer getCustomerInfo(String customerId) {
    LOGGER.debug("Getting customer with customerId = {}", customerId);
    return cache
        .findById(customerId)
        .orElseGet(() ->
            customerWebService.getCustomer(customerId)
                .map(mapper::toCustomer)
                .map(cache::save)
                .orElseThrow(() -> new NotFoundException(customerId, "customer"))
        );
  }

  public CustomerPreferences createCustomerPreferences(String customerId, SeatPreference seatPreference,
                                                       Integer classPreference, String profileName, Locale language) {
    LOGGER.debug("saveCustomerPreferences : " +
                    "seatPreference \"{}\", classPreference \"{}\" and profileName \"{}\"" +
                    " with locale\"{}\" for customer \"{}\"",
            seatPreference, classPreference, profileName, language, customerId);
    CustomerPreferences createCustomerPreferencesRequest = CustomerPreferences.builder()
            .customerId(customerId)
            .seatPreference(Optional.ofNullable(seatPreference).orElse(null))
            .classPreference(classPreference)
            .profileName(profileName)
            .language(language)
            .build();
    return database.save(createCustomerPreferencesRequest);
  }

  public List<CustomerPreferences> getCustomerPreferences(String customerId) {
    LOGGER.debug("getCustomerPreferences for customer: \"{}\"", customerId);
    List<CustomerPreferences> preferences = database.findByCustomerId(customerId);
    if (preferences == null || preferences.isEmpty()) {
      throw new NotFoundException(customerId, "customer");
    }
    return preferences;
  }
}
