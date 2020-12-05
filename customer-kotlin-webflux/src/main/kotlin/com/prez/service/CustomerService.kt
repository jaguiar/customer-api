package com.prez.service

import com.prez.cache.CustomerCacheRepository
import com.prez.db.CustomerPreferencesRepository
import com.prez.exception.NotFoundException
import com.prez.extension.toCustomer
import com.prez.model.Customer
import com.prez.model.CustomerPreferences
import com.prez.model.SeatPreference
import com.prez.ws.CustomerClient
import com.prez.ws.model.CreateCustomerPreferencesWSRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.Locale
import java.util.UUID

interface CustomerService {
  fun getCustomerInfo(customerId: String): Mono<Customer>

  fun createCustomerPreferences(
    customerId: String,
    seatPreference: SeatPreference,
    classPreference: Int,
    profileName: String,
    language: Locale?
  ): Mono<CustomerPreferences>

  fun getCustomerPreferences(customerId: String): Flux<CustomerPreferences>
}

@Component
class CustomerServiceImpl(
  val customerWebService: CustomerClient,
  val cache: CustomerCacheRepository,
  val database: CustomerPreferencesRepository
) : CustomerService {

  companion object {
    private val logger = LoggerFactory.getLogger(CustomerService::class.java)
  }

  override fun getCustomerInfo(customerId: String): Mono<Customer> {
    logger.debug("Getting customer with customerId=$customerId")
    return cache.findById(customerId)
      .switchIfEmpty(deferCallingCustomerWebService(customerId))
  }

  /**
   * Defer the execution of call to getCustomer web service. If you don't defer, the call will be executed in //
   * of the "previous" mono ( aka look in cache ) which is NOT what we want.
   * See https://stackoverflow.com/questions/54373920/mono-switchifempty-is-always-called if you want a more complete explanation
   */
  private fun deferCallingCustomerWebService(customerId: String): Mono<Customer> {
    return Mono.defer {
      customerWebService
        .getCustomer(customerId)
        .switchIfEmpty(Mono.error(NotFoundException(customerId, "customer")))
        .map { wsResponse -> wsResponse!!.toCustomer() }
        .flatMap { customer ->
          cache.save(customer)
            .defaultIfEmpty(false)
            .doOnSuccess { savedInCache ->
              if (savedInCache)
                logger.debug("Customer ${customer.customerId} saved in cache")
              else
                logger.error("COULD NOT SAVE ${customer.customerId} in cache")
            }
            // then we just return the customer
            .then(Mono.justOrEmpty(customer))
        }
    }
  }

  override fun createCustomerPreferences(
    customerId: String, seatPreference: SeatPreference,
    classPreference: Int, profileName: String, language: Locale?
  ): Mono<CustomerPreferences> {
    logger.debug(
      "createCustomerPreferences : " +
          "seatPreference ${seatPreference}, classPreference ${classPreference} and profileName ${profileName}" +
          " with locale ${language} for customer ${customerId}"
    )
    val createCustomerPreferencesRequest = CustomerPreferences(
      UUID.randomUUID().toString(),
      customerId,
      seatPreference,
      classPreference,
      profileName,
      language
    )
    return database.save(createCustomerPreferencesRequest)
  }

  override fun getCustomerPreferences(customerId: String): Flux<CustomerPreferences> {
    logger.debug("getCustomerPreferences for customer \"{}\"", customerId)
    return database.findByCustomerId(customerId)
  }
}

