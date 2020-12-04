package com.prez.api

import brave.SpanCustomizer
import com.prez.api.dto.CreateCustomerPreferencesRequest
import com.prez.api.dto.CustomerPreferencesProfileResponse
import com.prez.api.dto.CustomerResponse
import com.prez.extension.toCustomerPreferencesProfileResponse
import com.prez.extension.toCustomerResponse
import com.prez.model.CustomerPreferences
import com.prez.service.CustomerService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.apache.commons.lang3.LocaleUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/customers")
class CustomerController(private val customerService: CustomerService, private val spanCustomizer: SpanCustomizer) {

  @GetMapping(produces = ["application/json"])
  suspend fun getCustomer(principal: Principal): ResponseEntity<CustomerResponse> {
    spanCustomizer.tag("service", "GET /customers")
    LOGGER.info("Getting customer with customerId = {}", principal.name)
    val customer = customerService.getCustomerInfo(principal.name)
    return ResponseEntity.ok(customer.toCustomerResponse())
  }

  @PostMapping(produces = ["application/json"], value = ["/preferences"])
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  suspend fun createCustomerPreferences(
    @RequestBody @Validated validBody: CreateCustomerPreferencesRequest, principal: Principal
  ): CustomerPreferencesProfileResponse {
    spanCustomizer.tag("service", "POST /customers/preferences")
    LOGGER.info("CreateCustomerPreferences for user: {}, CustomerPreferences = {}", principal.name, validBody)
    val saved = customerService.saveCustomerPreferences(
      principal.name, validBody.seatPreference,
      validBody.classPreference, validBody.profileName,
      LocaleUtils.toLocale(validBody.language)
    )
    return saved.toCustomerPreferencesProfileResponse()
  }

  @FlowPreview
  @GetMapping(produces = ["application/json"], path = ["/preferences"])
  @ResponseBody
  fun getCustomerPreferences(principal: Principal): Flow<CustomerPreferencesProfileResponse> {
    spanCustomizer.tag("service", "GET /customers/preferences")
    LOGGER.info("getCustomerPreferences for user: {}", principal.name)
    return customerService.getCustomerPreferences(principal.name)
      .map(CustomerPreferences::toCustomerPreferencesProfileResponse)
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(CustomerController::class.java)
  }
}