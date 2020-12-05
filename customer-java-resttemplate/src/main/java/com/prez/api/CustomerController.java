package com.prez.api;

import static java.util.stream.Collectors.toList;

import brave.SpanCustomizer;
import com.prez.api.dto.CreateCustomerPreferencesRequest;
import com.prez.api.dto.CustomerPreferencesProfileResponse;
import com.prez.api.dto.CustomerResponse;
import com.prez.model.Customer;
import com.prez.model.CustomerPreferences;
import com.prez.service.CustomerService;
import java.security.Principal;
import java.util.List;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

  private final CustomerService customerService;

  private final SpanCustomizer spanCustomizer;

  public CustomerController(CustomerService customerService, SpanCustomizer spanCustomizer) {
    this.customerService = customerService;
    this.spanCustomizer = spanCustomizer;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<CustomerResponse> getCustomer(Principal principal) {
    spanCustomizer.tag("service", "GET /customers");
    LOGGER.info("Getting customer with customerId = {}", principal.getName());
    Customer customer = customerService.getCustomerInfo(principal.getName());
    return ResponseEntity.ok(CustomerResponse.of(customer));
  }

  @PostMapping(produces = "application/json", value = "/preferences")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public CustomerPreferencesProfileResponse createCustomerPreferences(
      @RequestBody @Validated final CreateCustomerPreferencesRequest validBody, Principal principal) {
    spanCustomizer.tag("service", "POST /customers/preferences");
    LOGGER.info("CreateCustomerPreferences for user: {}, CustomerPreferences = {}", principal.getName(), validBody);
    final CustomerPreferences saved =
        customerService.saveCustomerPreferences(principal.getName(), validBody.getSeatPreference(),
            validBody.getClassPreference(), validBody.getProfileName(),
            LocaleUtils.toLocale(validBody.getLanguage()));
    return CustomerPreferencesProfileResponse.of(saved);
  }

  @GetMapping(produces = "application/json", path = "/preferences")
  @ResponseBody
  public List<CustomerPreferencesProfileResponse> getCustomerPreferences(Principal principal) {
    spanCustomizer.tag("service", "GET /customers/preferences");
    LOGGER.info("getCustomerPreferences for user: {}", principal.getName());
    return customerService.getCustomerPreferences(principal.getName())
        .stream()
        .map(CustomerPreferencesProfileResponse::of)
        .collect(toList());
  }

}
